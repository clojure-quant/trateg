(ns ta.warehouse.overview
  (:require
   [tablecloth.api :as tc]
   [ta.warehouse :as wh]))

(defn load-datasets [w frequency symbols]
  (->> symbols
       (map (fn [symbol]
              (-> (wh/load-symbol w frequency symbol)
                  (tc/add-column :symbol symbol)
                  ;add-year-and-month-date-as-instant
                  #_(tc/add-column :return #(-> %
                                                :close
                                                returns)))))))

(defn concatenate-datasets [seq-ds-bar]
  (->> seq-ds-bar
       (apply tc/concat)))

(defn overview-view [ds-concatenated
                     {:keys [grouping-columns pivot?]
                      :or {grouping-columns [:symbol]
                           pivot? false}}]
  (-> ds-concatenated
      (tc/group-by grouping-columns)
      (tc/aggregate {:count tc/row-count
                     :first-date (fn [ds]
                                   (->> ds
                                        :date
                                        first))
                     :last-date (fn [ds]
                                  (->> ds
                                       :date
                                       last))
                     :min (fn [ds]
                            (->> ds
                                 :close
                                 (apply min)))
                     :max (fn [ds]
                            (->> ds
                                 :close
                                 (apply max)))})
      ((if pivot?
         #(tc/pivot->wider % :symbol [:min :max :count])
         identity))))

(defn warehouse-overview [w frequency & options]
  (let [options (if options options {})
        symbols (wh/symbols-available w frequency)
        datasets (load-datasets w frequency symbols)]
    (-> datasets
        concatenate-datasets
        (overview-view options))))

(comment

  (def w {:series "../db/crypto/"})
  (def w {:series "../db/random/"})

  (wh/symbols-available w "D")

  (warehouse-overview w "D")
  (warehouse-overview w "15")

;  
  )