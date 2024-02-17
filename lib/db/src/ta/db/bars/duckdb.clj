(ns ta.db.bars.duckdb
  (:require
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [clojure.java.io :as java-io]
   [tablecloth.api :as tc]
   [tmducken.duckdb :as duckdb]
   [tick.core :as tick]
   [ta.db.bars.protocol :refer [bardb]]
   ))

;; https://github.com/techascent/tmducken

(defn- exists-db? [path]
  (.exists (java-io/file path)))

(defn duckdb-start [db-filename]
  (duckdb/initialize! {:duckdb-home "./binaries"})
  (let [new? (exists-db? db-filename)
        db (duckdb/open-db db-filename)
        conn (duckdb/connect db)]
    {:db db
     :conn conn
     :new? new?}))

(defn duckdb-stop [{:keys [db conn] :as session}]
  (duckdb/disconnect conn))

;; bar-category

(defn bar-category->table-name [[calendar interval]]
  (str (name calendar) "_" (name interval)))

;; work with duckdb

(defn append-bars [session {:keys [calendar]} ds]
  (let [table-name (bar-category->table-name calendar)
        ds (tc/set-dataset-name ds table-name)]
    (info "duckdb append-bars # " (tc/row-count ds))
    (info "session: " session)
    (duckdb/insert-dataset! (:conn session) ds)))

(defn keywordize-columns [ds]
  (tc/rename-columns
   ds
   {"date" :date
    "open" :open
    "high" :high
    "low" :low
    "close" :close
    "volume" :volume
    "asset" :asset
    "epoch" :epoch
    "ticks" :ticks}))

(defn sql-query-bars-for-asset [calendar asset]
  (let [table-name (bar-category->table-name calendar)]
    (str "select * from " table-name " where asset = '" asset "' order by date")
  ))


(defn get-bars-full [session calendar asset]
    (debug "get-bars " asset)
    (let [query (sql-query-bars-for-asset calendar asset)]
    (-> (duckdb/sql->dataset (:conn session) query)
        (keywordize-columns))))

(defn sql-query-bars-for-asset-since [calendar asset since]
  (let [table-name (bar-category->table-name calendar)]
    (str "select * from " table-name 
         " where asset = '" asset "'" 
         " and date > '" since "'"
         " order by date")))

(defn get-bars-since [session calendar asset since]
    (debug "get-bars-since " asset since)
    (let [query (sql-query-bars-for-asset-since calendar asset since)]
      (-> (duckdb/sql->dataset (:conn session) query)
          (keywordize-columns))))
  

(defn sql-query-bars-for-asset-window [calendar asset dstart dend]
  (let [table-name (bar-category->table-name calendar)]
    (str "select * from " table-name
         " where asset = '" asset "'"
         " and date >= '" dstart "'"
         " and date <= '" dend "'"
         " order by date")))

(defn get-bars-window [session calendar asset dstart dend]
  (debug "get-bars-window " asset dstart dend)
  (let [query (sql-query-bars-for-asset-window calendar asset dstart dend)]
    (debug "sql-query: " query)
    (-> (duckdb/sql->dataset (:conn session) query)
        (keywordize-columns))))

(defn get-bars 
  "returns bars for asset/calendar + window"
  [session {:keys [asset calendar]} {:keys [start end] :as window}]
  (cond  
    (and start end)
    (get-bars-window session calendar asset start end)

    start
    (get-bars-since session calendar asset start)  
    
    :else 
    (get-bars-full session calendar asset)
    ))

(defn delete-bars [session]
  (duckdb/sql->dataset
   (:conn session)
   (str "delete from bars")))

 ;; CREATE INDEX s_idx ON films (revenue);


(defn now []
  (-> (tick/now)
      ;(tick/date-time)
      (tick/instant)))

(defn empty-ds [calendar]
  (let [table-name (bar-category->table-name calendar)]
    (-> (tc/dataset [{:open 0.0 :high 0.0 :low 0.0 :close 0.0
                      :volume 0.0 ; crypto volume is double.
                      :asset "000"
                      :date (now)
                      :epoch 0 :ticks 0}])
        (tc/set-dataset-name table-name))))


(defn create-table [session calendar]
  (let [ds (empty-ds calendar)]
    (duckdb/create-table! (:conn session) ds)))

(defn init-tables [session]
  (let [exists? (:new? session)]
    (when (not exists?)
      (println "init duck-db tables")
      (doall (map (partial create-table session)
                  [[:us :m]
                   [:us :h]
                   [:us :d]])))))

(defrecord bardb-duck [state]
  bardb
  (get-bars [this opts window]
    (info "this: " this)
    (get-bars (:state this) opts window))
  (append-bars [this opts ds-bars]
    (info "this: " this)
    (append-bars (:state this) opts ds-bars)))
 
(defn start-bardb-duck [opts]
  (let [state (duckdb-start opts)]
    (bardb-duck. state)))

(defn stop-bardb-duck [state]
  (duckdb-stop state))


(comment
  (require '[modular.system])
  (def db (:duckdb modular.system/system))
  (def db (duckdb-start "../../output/duckdb/bars"))
  db

  (bar-category->table-name [:us :m])
  (create-table db [:us :m])
  (create-table db [:us :h])



  (require '[tech.v3.dataset :as ds])
  (def stocks
    (ds/->dataset "https://github.com/techascent/tech.ml.dataset/raw/master/test/data/stocks.csv"
                  {:key-fn keyword
                   :dataset-name :stocks}))
  stocks
  (tc/info stocks)


  (get-bars db [:us :m] "EUR/USD")
  (get-bars db [:us :m] "ETHUSDT")

  (sql-query-bars-for-asset-since 
   [:us :m] "EUR/USD" "2024-01-26T19:35:00Z") 

  (require '[tick.core :as t])
  (def dt (t/instant "2024-01-26T19:35:00Z"))
  dt
  
  (get-bars-since db [:us :m] "EUR/USD" "2024-01-26T19:35:00Z" )
  (get-bars-since db [:us :m] "EUR/USD" "2024-01-26 19:35:00")
  (get-bars-since db [:us :m] "EUR/USD" dt)


  (def dt2 (t/instant "2024-01-26T16:35:00Z"))
  (get-bars-since db [:us :m] "EUR/USD" dt2)


  (get-bars-window db [:us :m] "EUR/USD" 
                   "2024-01-26T19:35:00Z"
                   "2024-01-26T19:45:00Z")

    (get-bars-window db [:us :m] "ETHUSDT"
                   "2024-01-29T18:56:00Z"
                   "2024-01-29T19:00:00Z")
  
   (get-bars-window db [:us :m] "ETHUSDT"
                 (tick/instant "2024-01-29T18:56:00Z")
                 (tick/instant "2024-01-29T19:00:00Z"))



  (get-bars-since duckdb [:us :m] "EUR/USD" time)
  (get-bars-since duckdb [:us :m] "EUR/USD" (str time))
  (t/inst time)
  (format-date time)
  
  (str time)
  
  (t/inst "2023-01-01 0:0:0")

  (now)
  empty-ds
  (tc/info empty-ds)
  (init-tables db)

  (duckdb/create-table! (:conn db) empty-ds)
  (duckdb/insert-dataset! (:conn db) empty-ds)

  (get-bars db "000")

  (get-bars db "EUR/USD")
  (get-bars db "USD/JPY")


  (exists-db?  "../../output/duckdb/bars")


  (duckdb/insert-dataset! db stocks)
  (ds/head (duckdb/sql->dataset db "select * from stocks"))
  (def stmt (duckdb/prepare db "select * from stocks "))
  (stmt)

  (def r (stmt))

  r


  ;
  )

