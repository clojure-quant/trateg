(ns ta.warehouse
  (:require
   [clojure.string :refer [includes? lower-case]]
   [clojure.java.io :as java-io]
   [clojure.edn :as edn]
   [taoensso.timbre :refer [debug info warnf error]]
   [tech.v3.io :as io]
   ;[taoensso.nippy :as nippy]
   [tablecloth.api :as tc]
   [ta.warehouse.split-adjust :refer [split-adjust]]))

(defonce config
  (atom {}))

(defn get-wh-path [kw]
  (get-in @config [:series kw]))

(defn init [settings]
  (info "wh init: " settings)
  (reset! config settings)
  settings)

;; lists

(defn load-list [name]
  (println "loading list: " name)
  (->> (str (:list @config) name ".edn")
       slurp
       edn/read-string
       (map :symbol)))

(defn load-list-full [name]
  (try
    (->> (str (:list @config) name ".edn")
         slurp
         edn/read-string)
    (catch Exception _
      (error "Error loading List: " name)
      [])))

(defn load-lists-full [names]
  (->> (map load-list-full names)
       (apply concat)
       (into [])))

(defn symbollist->dict [l]
  (let [s-name (juxt :symbol :name)
        dict (into {} (map s-name l))]
    dict))

(defn init-lookup [names]
  (let [l (load-lists-full names)
        d (symbollist->dict l)]
    {:lookup (fn [s]
               (if-let [n (get d s)]
                 n
                 (str "Unknown-" s)))
     :search (fn [q]
               (let [q (lower-case q)]
                 (filter (fn [{:keys [name symbol]}]
                           (includes? (lower-case name) q))

                         l)))}))

(comment
  (load-list-full "fidelity-select")

  ((juxt :symbol :name) {:symbol "s" :name "n"})

  (-> (load-list-full "bonds")
      (map (juxt :symbol :name)))

  (load-lists-full ["fidelity-select"
                    "bonds"
                    "commodity-industry"
                    "commodity-sector"
                    "currency"
                    "equity-region"
                    "equity-region-country"
                    "equity-sector-industry"
                    "equity-style"
                    "test"])

  (->  (load-lists-full ["fidelity-select" "bonds"])
       (symbollist->dict))

  (let [{:keys [lookup search]} (init-lookup ["fidelity-select" "bonds" "test"])]
    [(lookup "MSFT")
     (search "PH")])

; 
  )

; timeseries - name

(defn save-ts [wkw ds name]
  (let [p (get-wh-path wkw)
        s (io/gzip-output-stream! (str p name ".nippy.gz"))]
    (info "saving series " name " count: " (tc/row-count ds))
    (io/put-nippy! s ds)))

(defn load-ts [wkw name]
  (let [p (get-wh-path wkw)
        s (io/gzip-input-stream (str p name ".nippy.gz"))
        ds (io/get-nippy s)]
    (debug "loaded series " name " count: " (tc/row-count ds))
    ds))

; timeseries - symbol + frequency

(defn make-filename [frequency symbol]
  (str symbol "-" frequency))

(defn load-symbol [w frequency s]
  (-> (load-ts w (make-filename frequency s))
      split-adjust
      (tc/set-dataset-name (str s))
      (tc/add-column :symbol s)))

(defn save-symbol [w ds frequency symbol]
  (let [n (make-filename frequency symbol)]
    ;(info "saving: " n)
    (save-ts w ds n)))

; series in warehouse

(defn- filename->info [filename]
  (let [m (re-matches #"(.*)-(.*)\.nippy\.gz" filename)
        [_ symbol frequency] m]
    ;(errorf "regex name: %s cljs?: [%s]" name cljs?)
    {:symbol symbol
     :frequency frequency}))

(comment
  (filename->info "BTCUSD-15.nippy.gz")
 ; 
  )
(defn- dir? [filename]
  (-> (java-io/file filename) .isDirectory))

(defn symbols-available [w frequency]
  (let [dir (java-io/file (get-wh-path w))
        files (if (.exists dir)
                (into [] (->> (.list dir)
                              (remove dir?)
                              doall))
                (do
                  (warnf "path for: %s not found: %s"  dir)
                  []))]
    (debug "explore-dir: " files)
    ;(warn "type file:" (type (first files)) "dir?: " (dir? (first files)))
    (->> (map filename->info files)
         (remove #(nil? (:symbol %)))
         (filter #(= frequency (:frequency %)))
         (map :symbol))))

(comment

  (init {:list "../resources/etf/"
         :series  {:crypto "../db/crypto/"
                   :stocks "../db/stocks/"
                   :random "../db/random/"
                   :shuffled  "../db/shuffled/"}})

  @config
  (get-wh-path :crypto)

  (symbols-available :crypto "D")
  (load-symbol :crypto "D" "ETHUSD")

 ; 
  )