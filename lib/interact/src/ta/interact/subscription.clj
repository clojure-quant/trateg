(ns ta.interact.subscription
  (:require
   [de.otto.nom.core :as nom]
   [taoensso.timbre :as log :refer [tracef debug debugf info infof warn error errorf]]
   [nano-id.core :refer [nano-id]]
   [modular.system]
   [modular.ws.core :refer [send-all!  connected-uids]]
   [ta.algo.env.protocol :as algo]
   [ta.engine.protocol :as engine]
   [ta.interact.template :as t]
   [ta.viz.error :refer [error-render-spec]]))

(defonce subscriptions-a (atom {}))
(defonce results-a (atom {}))
(defonce visualizations-a (atom {}))
(defonce pushers-a (atom {}))

(defn push-viz-result [subscription-id result]
  (try
    (let [result (if (nom/anomaly? result)
                   (error-render-spec result)
                   result)]
      (warn "sending viz result: " subscription-id) ; putting result here will spam the log.
    ;(warn "result: " result)
      (send-all! [:interact/subscription {:subscription-id subscription-id :result result}]))
    (catch Exception ex
      (error "error in sending viz-result!"))))

(defn- get-fn [fun]
  (if (symbol? fun)
    (requiring-resolve fun)
    fun))

(defn create-viz-fn [e {:keys [id] :as template} mode]
  ;(info "create-viz-fn: " viz)
  (let [{:keys [viz viz-options]} (get template mode)
        viz-fn (get-fn viz)]
    (when viz-fn
      (fn [result]
        (if (nom/anomaly? result)
          result
          (try
            (warn "calculating visualization:" id " .. ")
            ;(warn "result: " result)
            (let [r (if viz-options
                      (viz-fn viz-options result)
                      (viz-fn result))]
              (warn "calculating visualization:" id " DONE!")
              r)
            (catch Exception ex
              (error "exception calculating visualization topic: " id)
              (error ex)
              (nom/fail ::algo-calc {:message "algo calc exception!"
                                     :location :visualize}))))))))

(defn subscribe [e {:keys [id algo key] :as template} mode]
  (let [subscription-id (nano-id 6)
        eng (algo/get-engine e)
        algo-results-a (algo/add-algo e algo)
        algo-result-a (if key (key algo-results-a)
                          algo-results-a)
        ;_ (info "algo-result-a: " algo-result-a)
        viz-fn (create-viz-fn e template mode)]
    (if viz-fn
      (let [viz-result-a (engine/formula-cell eng viz-fn [algo-result-a])
            pusher-a (engine/formula-cell eng #(push-viz-result subscription-id %) [viz-result-a])]
        (swap! subscriptions-a assoc subscription-id template)
        (swap! results-a assoc subscription-id algo-result-a)
        (swap! visualizations-a assoc subscription-id viz-result-a)
        (swap! pushers-a assoc subscription-id pusher-a)
        subscription-id)
      (do
        (error "could not create viz-fn for template: " id)
        nil))))

(defn subscribe-kw [env-kw template-id template-options mode]
  (let [e (modular.system/system env-kw)
        template (t/load-with-options template-id template-options)]
    (info "subscribing template: " template)
    (subscribe e template mode)))

(defn subscribe-live [template-id template-options mode]
  (info "subscribe-live template:" template-id "mode: " mode)
  (if-let [result (@visualizations-a template-id)]
    (push-viz-result template-id @result)
    (subscribe-kw :live template-id template-options mode)))

(defn unsubscribe [subscription-id]
  (when-let [s (get @subscriptions-a subscription-id)]
    (warn "unsubscribing subscription-id: " subscription-id)
    (let [e (modular.system/system :live)
          eng (algo/get-engine e)
          pusher-result-a (get @pushers-a subscription-id)
          viz-result-a (get @visualizations-a subscription-id)
          result-a (get @results-a subscription-id)]
      ;(warn "destroying pusher..")
      (engine/destroy-cell eng pusher-result-a)
      (swap! pushers-a dissoc subscription-id)
      ;(warn "destroying viz-result..")
      (engine/destroy-cell eng viz-result-a)
      (swap! visualizations-a dissoc subscription-id)
      ;(warn "destroying algo-result..")
      (engine/destroy-cell eng result-a)
      (swap! results-a dissoc subscription-id)
      ; done!
      (swap! subscriptions-a dissoc subscription-id))))

(comment

  (def s (atom {:mood "perfect"
                :env {:mode :live}
                :benchmark ["MSFT"
                            "MO"]
                :asset "SPY"}))

  (swap! s assoc :asset "QQQ")
  (swap! s assoc-in [:env :mode] :backtest)
  (swap! s assoc-in [:benchmark 0] "AAPL")

; 
  )

