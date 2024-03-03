(ns notebook.strategy.sentiment-spread.study
  "Sentiment Spreads
  Backtest of a strategy described in 
  https://cssanalytics.wordpress.com/2010/09/19/creating-an-ensemble-intermarket-spy-model-with-etf-rewinds-sentiment-spreads/
  The indicator according to Jeff Pietsch (who is the creator of ETF Rewind) is most valuable for intraday-trading as an indicator that captures the market’s sentiment towards risk assets. A positive spread or positive differential return implies that the market is willing to take risk and thus likely to go higher. By extension, the more spreads that are positive, 
  or the greater the sum of the spreads, the more likely the market will go up and vice versa"
  (:require
   [tablecloth.api :as tc]
   [ta.algo.backtest :refer [backtest-algo]]
   [notebook.strategy.sentiment-spread.vega :as v]
   ))

(def algo-spec {:type :time
                :algo 'notebook.strategy.sentiment-spread.algo/sentiment-spread
                :calendar [:us :d]
                :import :kibot
                :trailing-n 1000
                :market "SPY"
                :spreads [[:consumer-sentiment "XLY" "XLP"]
                          [:smallcap-speculative-demand "IWM" "SPY"]
                          [:em-speculative-demand "EEM" "EFA"]
                          [:innovation-vs-safehaven "XLK" "GLD"]
                          [:stocks-vs-bonds "SPY" "AGG"]
                          [:quality-yield-spreads "HYG" "AGG"]
                          [:yen-eur-currency "FXE" "FXY"]
                          ; 8th spread- VXX-VXZ – due to insufficient historical data.
                          ]})

(def sentiment-ds
  (backtest-algo :bardb-dynamic algo-spec))


@sentiment-ds


; correlation between factors and spx
; (stats/cor 'm spy :method "pearson" :use "pairwise.complete.obs")


(defn distribution [sentiment-ds]
  (-> sentiment-ds
      (tc/group-by :sentiment)
      (tc/aggregate
       {:count (fn [ds] (tc/row-count ds))})
      (tc/order-by :$group-name)))

(distribution @sentiment-ds)
;; => _unnamed [8 2]:
;;    
;;    | :$group-name | :count |
;;    |-------------:|-------:|
;;    |         -7.0 |      9 |
;;    |         -5.0 |     55 |
;;    |         -3.0 |    174 |
;;    |         -1.0 |    208 |
;;    |          1.0 |    211 |
;;    |          3.0 |    216 |
;;    |          5.0 |     76 |
;;    |          7.0 |     46 |


(def algo-spec-4000 (assoc algo-spec :trailing-n 4000))

(def sentiment-4000-ds
  (backtest-algo :bardb-dynamic algo-spec-4000))

(v/publish-vega @sentiment-4000-ds :sentiment)



(distribution @sentiment-1000-ds)

