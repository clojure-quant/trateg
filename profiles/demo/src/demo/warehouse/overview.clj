(ns demo.warehouse.overview
  (:require
   [net.cgrand.xforms :as x]
   [tech.v3.dataset :as tds]
   [tech.v3.datatype.functional :as dfn]
    [tablecloth.api :as tablecloth]
   ;[ta.data.date :refer [parse-date]]
   [ta.warehouse :as wh]
   ;[ta.series.indicator :as ind]
   ;[ta.backtest.core :as bt]
   ;[ta.backtest.chart :as c]
   [ta.data.date :refer  [->epoch]]
   [ta.viz.arrow :as a]
   [demo.env.warehouse :refer [w]]
   ;[demo.studies.helper.sma :refer [sma-study]]
[demo.studies.helper.experiments-helpers :as helpers]
   ))

(def ds1 (->
        (wh/load-ts w "MSFT")))

ds1

(a/publish-ds! ds1 :test)

^:R
[:p/vegalite
 {:width 800
  :height 600
  :spec {:data {:url "/api/arrow"
                :format {:type "arrow"}}
         :mark {:type "bar"
               ;:tooltip true
                :tooltip {:content "data"}}
         :encoding {:x {:field "date" :type "quantitative"}
                    :y {:field "close" :type "quantitative"}}}}]


(def symbols
  (wh/load-list w "fidelity-select"))


(defonce datasets
  (->> symbols
       (map (fn [symbol]
              (-> (wh/load-ts w symbol)
                  (tablecloth/add-column :symbol symbol)
                  (tablecloth/add-column :return #(-> %
                                                      :close
                                                      helpers/returns)))))))


(def concatenated-dataset
  (->> datasets
       (apply tablecloth/concat)))

(tablecloth/shape concatenated-dataset)

(-> concatenated-dataset
    (tablecloth/random 10))


(a/publish-ds! concatenated-dataset :test)


^:R
[:p/vegalite
 {:width 800
  :height 600
  :spec {:data {:url "/api/arrow"
                :format {:type "arrow"}}
         :mark {:type "line"
               ;:tooltip true
                :tooltip {:content "data"}}
         :encoding {:x {:field "date" :type "quantitative"}
                    :y {:field "close" :type "quantitative"}
                    :row {:field "symbol"}
                    }}}]