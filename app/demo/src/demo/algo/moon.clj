(ns demo.algo.moon
  (:require
   [tablecloth.api :as tc]
   [tech.v3.datatype :as dtype]
   [ta.algo.manager :refer [add-algo]]
   [astro.moon :refer [inst->moon-phase-kw phase->text]]
   [ta.tradingview.shape.core :as shapes]))

(defn add-moon-indicator [ds-bars _]
  (tc/add-column
   ds-bars
   :phase
   (dtype/emap inst->moon-phase-kw :object (:date ds-bars))))

(defn calc-moon-signal [phase]
  (if phase
    (case phase
      :i1 :flat
      :full :buy
      :hold)
    :hold))

(defn moon-signal [ds-bars options]
  (let [ds-study (add-moon-indicator ds-bars options)
        signal (into [] (map calc-moon-signal (:phase ds-study)))]
    (tc/add-columns ds-study {:signal signal})))

;; SHAPES 

(defn cell->shape [epoch value]
  (let [text (phase->text value)]
    (shapes/text epoch text)))

(comment
  (cell->shape 3 :full)
  (cell->shape 3 :new)
 ;
  )

(defn moon-phase-shapes [symbol frequency user-options epoch-start epoch-end]
  (shapes/algo-col->shapes
   "moon"
   symbol frequency user-options epoch-start epoch-end
   :phase cell->shape))

(defn fixed-shapes [symbol frequency user-options epoch-start epoch-end]
  [(shapes/line-vertical 1644364800) ; feb 9
   (shapes/line-vertical 1648944000) ; april 3
   (shapes/line-horizontal 350.55)
   (shapes/gann-square 1643846400 350.0 1648944000  550.0)])

(add-algo
 {:name "moon"
  :comment "very good - 2:1"
  :algo moon-signal
  :charts [nil ; {:trade "flags"}
           {:volume "column"}]
  :shapes moon-phase-shapes ; fixed-shapes
  :options {:w :stocks
            :symbol "SPY"
            :frequency "D"}})
