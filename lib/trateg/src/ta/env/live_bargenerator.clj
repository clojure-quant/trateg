(ns ta.env.live-bargenerator
  "the live-bargenerate is an environment in which algos can be run.
   An environment currently only provides get-series fn., but more
   can be added later.
   We can have multiple live environments, and multiple backtest environments.
   You can schedule fns that are run at the close of a bar-category.
   Outputs a stream of results-fn
   
   Manages subscriptiosn to a quote feed. 

   Inspection:
   (quote-snapshot state) - gets last quote of all currently subscribed quotes
   (unfinished-bar-snapshot state [:us :m]) - gets unfinished bars snapshot.
   Todo:
   - unsubscribe-quote
   - remove algo
   - stop live environment
   - 

   "
  (:require
   [taoensso.timbre :refer [trace debug info warn error]]
   [manifold.stream :as s]
   [tablecloth.api :as tc]
   [ta.warehouse.duckdb :as duck]
   [ta.tickerplant.bar-generator :as bg]
   [ta.quote.core :refer [subscribe quote-stream]]
   [ta.env.last-msg-summary :as summary]
   [ta.env.live.trailing-window-algo :refer [trailing-window-algo]]
   ))

(defn create-live-environment [feed duckdb]
  {:feed feed
   :duckdb duckdb
   :bar-categories (atom {})
   :env {:get-series (partial duck/get-bars-window duckdb)}
   :live-results-stream (s/stream)
   :summary-quote (summary/create-last-summary (quote-stream feed) :asset)})

(defn get-feed [state]
  (:feed state))

(defn get-result-stream [state]
  (:live-results-stream state))

(defn get-bar-categories [state]
  (keys @(:bar-categories state)))

(defn quote-snapshot [state]
  (summary/current-summary (:summary-quote state)))

;; bar-generator for bar-category
(defn get-existing-bar-category [{:keys [bar-categories] :as state} bar-category]
  (get @bar-categories bar-category))


(defn unfinished-bar-snapshot [state bar-category]
  (let [{:keys [bargenerator]} (get-existing-bar-category state bar-category)]
    (bg/current-bars bargenerator)))

        ;  [bar-category :results-stream]))

(defn category-result-stream [state bar-category]
  (get-in @(:bar-categories state) [bar-category :results-stream]))


(defn category-bar-time-stream [state bar-category]
  (get-in @(:bar-categories state) [bar-category :bar-close-stream]))


(defn category-onbar-stream [state bar-category]
  (get-in @(:bar-categories state) [bar-category :bars-finished-stream]))

(defn category-algos [state bar-category]
  (get-in @(:bar-categories state) [bar-category :algos]))

(defn add-algo-to-bar-category [state bar-category algo]
  (swap! (:bar-categories state) update-in [bar-category :algos] conj algo))

(defn select-valid-bars-ds [ds-bars]
  (when ds-bars
    (tc/select-rows ds-bars  #(:close %))))

(defn ds-has-rows [ds-bars]
  (if ds-bars
    (let [c (tc/row-count ds-bars)]
      (> c 0))
    false))

(comment 
    (def ds
    (-> {:close [10.0 20.0 nil]
         :asset ["MSFT" "QQQ" "SPX"]}
        tc/dataset))
  ds
  (select-valid-bars-ds ds)
  (select-valid-bars-ds nil)
  
  (ds-has-rows ds)
  (ds-has-rows nil)
;
  )
  

(defn save-finished-bars [duckdb]
  (fn [{:keys [time category ds-bars] :as msg}]
    (try
      (info "save finished bars " time category " ... ")
      (let [ds-bars (select-valid-bars-ds ds-bars)]
        (if (ds-has-rows ds-bars)
          (duck/append-bars duckdb category ds-bars)
          (warn "not saving finished bars - ds-bars is not valid!")))
      (catch Exception ex
        (error "generated bars save exception!")
        (error "bars that could not be saved: " ds-bars)
        (bg/print-finished-bars msg)))
     msg))

(defn calc-algo [env {:keys [algo algo-opts]} time]
  (try
    (debug "calculating algo: " algo " time: " time)
    (let [result (algo env algo-opts time)]
      (debug "calculating algo: " algo " time: " time " result: " result)
      result)
    (catch Exception ex
      (error "algo-calc exception " algo time ex)
      nil)))

(defn calculate-on-bar-close
  "input: time-msg 
   output: bar-done-msg
   side effect: calculates algos and puts results to result steam"
  [{:keys [env] :as state}
   bar-category
   {:keys [time category ds-bars] :as msg}]
  (info "calculate-on-bar-close " bar-category time)
  (try
    (let [result-stream (category-result-stream state bar-category)
          algos (category-algos state bar-category)]
      (info "algos: " algos)
      (doall (map (fn [algo]
                    (let [result (calc-algo env algo time)]
                      (when result
                        (debug "putting result to result stream: " result)
                        (s/put! result-stream result))))
                  algos)))
      (catch Exception ex
        (error "exception calculate-on-bar-close " bar-category)
        (error "ex: " ex)))
  msg)

(defn connect-feed-with-bargenerator [bargen feed]
  (info "connecting feed with bargenerator")
  (let [stream (quote-stream feed)
        process-tick (fn [tick]
                       ;(info "bargen is processing tick: " tick)
                       (bg/process-tick bargen tick)
                       ;(info "bargen is processing tick: " tick " FINISHED!")
                       )]
    (s/consume process-tick stream)))

(defn create-bar-category [{:keys [duckdb feed] :as state} 
                            bar-category]
  (info "add new bar category: " bar-category)
  (let [bargen (bg/bargenerator-start bar-category)
        bar-close-stream (bg/bar-close-stream bargen)
        bars-finished-stream (s/map (save-finished-bars duckdb) bar-close-stream)
        ;results-stream (s/map (partial calculate-on-bar-close env bar-category)
        ;                      bars-finished-stream)
        results-stream (s/stream)
        data {:bargenerator bargen
              :calc-fns []
              :bar-category bar-category
              :bar-close-stream bar-close-stream
              :bars-finished-stream bars-finished-stream
              :results-stream results-stream}
        live-results-stream (get-result-stream state)]
    (swap! (:bar-categories state) assoc bar-category data)
    ;(info "connecting streams...")
    (s/consume (partial calculate-on-bar-close state bar-category) bars-finished-stream)
    (connect-feed-with-bargenerator bargen feed)
    (s/connect results-stream live-results-stream)
    ;(info "connecting streams... done!")
    data))

(defn get-bar-category [state bar-category]
  (or (get-existing-bar-category state bar-category)
      (create-bar-category state bar-category)))

(defn add [state algo-wrapped]
  (let [{:keys [algo-opts _algo]} algo-wrapped
        {:keys [bar-category asset]} algo-opts]
    (get-bar-category state bar-category)
    (add-algo-to-bar-category state bar-category algo-wrapped)
  (if asset
    (let [feed (get-feed state)]
      (info "added algo with asset [" asset "] .. subscribing..")
      (subscribe feed asset))
    (warn "added algo without asset .. not subscribing!"))))

(defn add-bar-strategy [state algo-bar-strategy-wrapped]
  (add state (trailing-window-algo algo-bar-strategy-wrapped)))

;; see in demo notebook.live.live-bargenerator


(comment
  (require '[modular.system])
  (def live (modular.system/system :live))

  live

  ;(def bar-category [:forex :m])
  ;(def bar-category [:us :m])
  ;(def bar-category [:eu :m])
  (def bar-category [:crypto :m])

  ; add (create) a calculation category.
  ;(get-bar-category live bar-category)

  (category-algos live bar-category)

  ;; simulate a bargenerator event
  (def time-stream (category-bar-time-stream live bar-category))
  time-stream
  (s/put! time-stream {:time :now
                       :category bar-category
                       :ds-bars nil})


  (def c-result-stream (category-result-stream live bar-category))
  (s/put! c-result-stream {:result :test})

  (category-bar-time-stream live bar-category)






  (calculate-on-bar-close live bar-category :now)


  (get-result-stream live)

  (get-bar-categories live)

  (get-feed live)

  (let [feed (get-feed live)]
    (subscribe feed "USD/JPY"))


  (-> (get-bar-category live bar-category)
      :bar-close-stream 
      (s/take!))
  

  (-> (get-bar-category live bar-category)
      :bar-close-stream
      (s/put! {:time :now
               :category bar-category
               :ds-bars nil}))
  


  
  (category-result-stream live bar-category)
  (category-algos live bar-category)
  

 ; 
  )