(ns ta.backtest.roundtrip-stats
  (:require
   [clojure.set]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as tds]
   [ta.helper.stats :refer [mean]]
   [ta.backtest.drawdown :refer [max-drawdown]]))

(defn calc-roundtrip-stats [ds-roundtrips group-by]
  (-> ds-roundtrips
      (tc/group-by group-by)
      (tc/aggregate {:bars (fn [ds]
                             (->> ds
                                  :bars
                                  (apply +)))
                     :trades (fn [ds]
                               (->> ds
                                    :trade
                                    (remove nil?)
                                    count))
                     ; log
                     :pl-log-cum (fn [ds]
                                   (->> ds
                                        :pl-log
                                        (apply +)))

                     :pl-log-mean (fn [ds]
                                    (->> ds
                                         :pl-log
                                         mean))

                     :pl-log-max-dd (fn [ds]
                                      (-> ds
                                          (tc/->array :pl-log)
                                          max-drawdown))} {:drop-missing? false})
      (tc/set-dataset-name (tc/dataset-name ds-roundtrips))))

(defn position-stats [backtest-result]
  (let [ds-roundtrips (:ds-roundtrips backtest-result)]
    (as-> ds-roundtrips x
      (calc-roundtrip-stats x [:position])
      (tds/mapseq-reader x)
      (map (juxt :position identity) x)
      (into {} x))))

(defn win-loss-stats [backtest-result]
  (let [ds-roundtrips (:ds-roundtrips backtest-result)]
    (as-> ds-roundtrips x
      (calc-roundtrip-stats x [:win])
      (tds/mapseq-reader x)
      (map (juxt :win identity) x)
      (into {} x)
      (clojure.set/rename-keys x {true :win
                                  false :loss}))))

(defn win-loss-performance-metrics [win-loss-stats]
  (let [win (:win win-loss-stats)
        loss (:loss win-loss-stats)
        pl-log-cum (+ (:pl-log-cum win)
                      (:pl-log-cum loss)) ; loss is negative, so add
        trade-count-all (+ (:trades win) (:trades loss))
        win-prct  (/ (:trades win) trade-count-all)
        loss-prct (- 1.0 win-prct)]
    {:trades trade-count-all
     :pl-log-cum pl-log-cum
     :pf (/  (* win-prct (:pl-log-mean win))
             (* loss-prct (- 0 (:pl-log-mean loss))))
     :avg-log (/ pl-log-cum  (float trade-count-all))
     :avg-win-log (:pl-log-mean win)
     :avg-loss-log (:pl-log-mean loss)
     :win-nr-prct (* 100.0 win-prct)
     :avg-bars-win  (* 1.0 (/ (:bars win) (:trades win)))
     :avg-bars-loss  (* 1.0 (/ (:bars loss) (:trades loss)))}))

(defn roundtrip-performance-metrics [backtest-result]
  (let [ds-roundtrips (:ds-roundtrips backtest-result)
        wl-stats (win-loss-stats backtest-result)
        metrics (win-loss-performance-metrics wl-stats)
        metrics-ds (tc/dataset metrics)]
    (-> metrics-ds
        (tc/set-dataset-name "rt-stats")
        (tc/add-column :p (tc/dataset-name ds-roundtrips)))))

; {:drop-missing? false} as an option

(def cols-rt-stats [:p
                    :trades
                    :pf :pl-log-cum :avg-log
                    :win-nr-prct
                    :avg-win-log :avg-loss-log
                    :avg-bars-win :avg-bars-loss])

(defn backtests->performance-metrics [backtest-results]
  (as-> (map roundtrip-performance-metrics backtest-results) x
    (apply tc/concat x)
    (tc/select-columns x cols-rt-stats)
       ;(reduce tc/append (tc/dataset {}))
    ))

