(ns ta.tradingview.goldly.algo.context)

(defn create-algo-context [algo opts]
  (let [data (atom nil)
        input (atom {:algo algo
                     :opts opts})
        context {:input input
                 :data data}]
    context))

(defn get-algo-input [algo-ctx]
  (-> algo-ctx :input deref))

(defn get-algo-name [algo-ctx]
  (-> algo-ctx :input deref :algo))

(defn get-data [algo-ctx]
  (-> algo-ctx :data deref))

(defn get-chart-spec [algo-ctx]
  (-> algo-ctx :data deref :charts))

(defn get-pane-columns [algo-ctx pane-id]
   (let [chart-spec (get-chart-spec algo-ctx)
         pane-spec (get chart-spec pane-id)]
    (keys pane-spec)))

(defn get-chart-series [algo-ctx]
  (-> algo-ctx :data deref :ds-study))

(defn get-chart-series-window [algo-ctx period]
  (let [{:keys [from to first? count-back]} period
        series (get-chart-series algo-ctx)
        in-window? (fn [{:keys [epoch]}]
                     (and (>= epoch from) (<= epoch to)))]
     (if first?
        (take-last count-back series)
        (filter in-window? series))))


(defn get-pane-data [algo-ctx pane-id time-index]
   (let [data (get-chart-series algo-ctx)
         cols (get-pane-columns algo-ctx pane-id)
         row (-> (filter #(= time-index (:index %)) data) first)]
     (if (empty? cols)
         []
         (let [get-data (apply juxt cols)]
           (get-data row)))))

(defn set-algo-data [algo-ctx data]
  (reset! (:data algo-ctx) data))
         