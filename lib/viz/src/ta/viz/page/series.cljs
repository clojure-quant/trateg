(ns ta.viz.page.series
  (:require
   [reagent.core :as r]
   [ta.tradingview.goldly.view.aggrid :refer [bars-table]]
   [ta.viz.lib.loader :refer [clj->a]]
   [ta.viz.lib.ui :refer [link-href]]
   [ta.viz.view.tsymbol :refer [symbol-picker]]
   ))


(defn series-view [s f]
  (let [bars (r/atom {})]
    (fn [s f]
      (clj->a bars 'ta.warehouse.load/load-series {:symbol s :frequency f})
      [:div.w-full.h-full
       ;[:h1.text-bold.bg-green-500 "Bars for symbol: " s " f: " f]
       (case (:status @bars)
         :loading [:p "loading"]
         :error [:p "error!"]
         :data [:div.w-full.h-full 
                [bars-table (:data @bars)]]
         [:p "unknown: status:" (pr-str @bars)])])))

(defn pickable-series-view []
  (let [state (r/atom {:symbol "MSFT"})]
    (fn []
      [:div.flex.flex-row.h-full.w-full
        [symbol-picker state [:symbol]]
        ;[:p "symbol:" (:symbol @state)]
        [series-view (:symbol @state) "D"]])))



(defn series-page [_route]
  [:div.h-screen.w-screen.bg-red-500
   [:div.flex.flex-col.h-full.w-full
      ;[:div.flex.flex-row.bg-blue-500
    [link-href "/" "main"]
    ;[series-view  "BTCUSD" "D"]
    [pickable-series-view]
   ;    ]
    ]])

