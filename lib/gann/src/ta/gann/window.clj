(ns ta.gann.window
  (:require
   [taoensso.timbre :refer [trace debug info warnf error]]
   [cljc.java-time.duration :as duration]
   [tick.core :as tick :refer [>>]]
   [tick.alpha.interval :as t.i]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as tds]
   [tech.v3.datatype.functional :as dfn]
   [ta.helper.date :refer [parse-date now-datetime]]
   [ta.warehouse :refer [exists-symbol? load-symbol]]
   [ta.warehouse.symbol-db :refer [determine-wh]]
   [ta.gann.box :refer [get-boxes-in-window make-root-box zoom-out zoom-in]]
   [ta.gann.db :refer [get-root-box]]))

;; get prices

(defn row-in-range [dt-start dt-end {:keys [date] :as row}]
  (and (tick/>= date dt-start)
       (tick/<= date dt-end)))

(defn get-prices [wh symbol dt-start dt-end]
  (when (exists-symbol? wh "D" symbol)
    (let [ds (-> (load-symbol wh "D" symbol)
                 (tc/select-rows
                  (partial row-in-range dt-start dt-end))
                 (tc/select-columns [:date :close]))
          ds-log (tc/add-columns ds {:close-log (dfn/log10 (:close ds))})]
      {:series (mapv (juxt :date :close-log) (tds/mapseq-reader ds-log))
       :px-min (apply min (:close-log ds-log))
       :px-max (apply max (:close-log ds-log))
       :count (count (:close-log ds-log))})))

(comment
  (-> (get-prices :crypto "BTCUSD" (parse-date "2021-01-01") (parse-date "2021-12-31"))
      (dissoc :series))

  (-> (get-prices :stocks "GLD" (parse-date "2021-01-01") (parse-date "2021-12-31"))
      (dissoc :series))

  (-> (get-boxes-in-window (get-root-box "GLD") (parse-date "2004-01-01") (parse-date "2021-12-31")
                           (Math/log10 2000) (Math/log10 3000))
      (clojure.pprint/print-table))

;
  )
(comment  ; in case you dont want to load instrument data below:

  (defn get-close-prices-test [symbol dt-start dt-end]
    [[(parse-date "2021-03-01") (Math/log10 50000)]
     [(parse-date "2021-07-01") (Math/log10 40000)]
     [(parse-date "2021-08-01") (Math/log10 60000)]])
 ; 
  )

(defn get-gann-data [{:keys [s wh dt-start dt-end root-box]
                      :or {root-box (get-root-box s)
                           wh (determine-wh s)
                           dt-start (parse-date "2021-01-01")
                           dt-end (parse-date "2021-12-31")}}]
  (info "get-gann-data symbol: " s "wh: " wh)
  (let [dt-start (if (string? dt-start) (parse-date dt-start) dt-start)
        dt-end (if (string? dt-end) (parse-date dt-end) dt-end)
        data (get-prices wh s dt-start dt-end)  ; vec of float
        px-min (:px-min data)  ;(Math/log10 3000) 
        px-max (:px-max data) ; (Math/log10 70000) ; ; 
        close-series (:series data)
        boxes (if root-box
                (get-boxes-in-window root-box dt-start dt-end px-min px-max)
                [])]
    (when root-box
      {:symbol s
       :px-min px-min
       :px-max px-max
       :dt-start dt-start
       :dt-end dt-end
       :boxes boxes
       :close-series close-series})))

(defn get-gann-boxes [opts]
  (->> opts
       get-gann-data
       :boxes
       ;(map #(dissoc % :dt))
       ))

(comment

  (load-symbol :crypto "D" "ETHUSD")
  (exists-symbol? :crypto "D" "BAD")
  (load-symbol :crypto "D" "BAD") ; throws exception
  (determine-wh "ETHUSD")
  (determine-wh "BAD")

  (get-root-box "BTCUSD")
  (get-root-box "BAD")

  (get-gann-data {:s "BTCUSD"})
  (get-gann-data {:s "BAD"})

  (get-gann-boxes {:s "BTCUSD"})
  (get-gann-boxes {:s "BAD"})

  (-> (get-gann-data {:s "BTCUSD"})
      ;(dissoc :close-series)
      )
  (-> (get-gann-data {:s "GLD"
                      :wh :stocks
                      :dt-start (parse-date "2021-01-01")
                      :dt-end (parse-date "2021-12-31")})
      (dissoc :close-series))

;  
  )
