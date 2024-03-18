(ns ta.trade.roundtrip-backtest.signal2trade
   (:require
   [tablecloth.api :as tc]
   [ta.helper.ago :refer [xf-ago-pair]]))


(defn- running-index-vec [ds]
  (range 0  (tc/row-count ds)))

(defn- add-running-index [ds]
  (tc/add-column ds :index (running-index-vec ds)))

; signal :buy :sell :hold  
; one or more buy signals mean we do want to be long.
; vice versa sell signal.

(defn- xf-signal->position [xf]
  (let [position (atom :flat)]
    (fn
      ;; SET-UP
      ([]
       (reset! position :flat)
       (xf))
     	;; PROCESS
      ([result input]
       (xf result (case input
                    :buy (reset! position :long) ;reset! returns the new value
                    :sell (reset! position :short)
                    :flat (reset! position :flat)
                    @position)))
      ;; TEAR-DOWN
      ([result]
       (xf result)))))

(defn- position-change->trade [[position-prior position-current]]
  (if (= position-prior position-current)
    nil
    (case position-current
      :long :buy
      :short :sell
      :flat :flat
      nil)))

(defn signal->position [signal-seq]
  (into [] xf-signal->position
        signal-seq))

(defn signal->trade [signal-seq]
  (into [] (comp
            xf-signal->position
            xf-ago-pair
            (map position-change->trade))
        signal-seq))

(defn- xf-trade->trade-no [xf]
  (let [no (atom 0)]
    (fn
      ;; SET-UP
      ([]
       (reset! no 0)
       (xf))
     	;; PROCESS
      ([result input]
       (when (or (= input :buy) (= input :sell) (= input :flat))
         (swap! no inc))
       (xf result @no))
      ;; TEAR-DOWN
      ([result]
       (xf result)))))

(defn- trade->trade-no [trade-seq]
  (into [] xf-trade->trade-no
        trade-seq))

(defn trade-signal [ds]
  (if-let [signal (:signal ds)]
    (let [trade (signal->trade signal)
          trade-no (trade->trade-no trade)
          position (signal->position signal)]
      (tc/add-columns ds {:index (running-index-vec ds)
                          :signal signal
                          :trade trade
                          :trade-no trade-no
                          :position position}))
    ds))