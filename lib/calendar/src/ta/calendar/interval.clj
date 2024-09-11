(ns ta.calendar.interval
  (:require
   [tick.core :as t]
   [ta.calendar.interval.year :as year]
   [ta.calendar.interval.month :as month]
   [ta.calendar.interval.week :as week]
   [ta.calendar.interval.day :as day]
   [ta.calendar.interval.intraday :as intraday]
   [ta.calendar.calendars :refer [calendars]]
   [ta.calendar.helper :refer [intraday?]]
   [ta.helper.date :refer [align-field now-in-zone]]))

(defn now-calendar [{:keys [timezone] :as calendar}]
  (now-in-zone timezone))

(defn working-days-count [period]
  ; TODO
  )

(defn get-calendar-day-duration [calendar-kw]
  (let [{:keys [open close] :as calendar} (calendar-kw calendars)]
    (cond
      (t/< open close) (t/divide (t/between open close)
                                 (t/new-duration 1 :seconds))
      (t/> open close) (t/divide (t/- (t/new-duration 1 :days) (t/between close open))
                                 (t/new-duration 1 :seconds))
      (t/= open close) (t/divide (t/new-duration 1 :days)
                                 (t/new-duration 1 :seconds)))))

(defn get-calendar-month-duration [calendar-kw date]
  ; TODO
  ;(let [days (working-days-count )
  ;      day-duration (get-calendar-day-duration calendar-kw)]
  ;  (* days day-duration))
  )

(defn gen-intraday-step-fn [n unit]
  ; close
  {:next-close     (fn ([calendar] (intraday/next-close-dt calendar n unit))
                     ([calendar dt] (intraday/next-close-dt calendar n unit dt)))

   :prior-close    (fn ([calendar] (intraday/prior-close-dt calendar n unit))
                     ([calendar dt] (intraday/prior-close-dt calendar n unit dt)))

   :current-close  (fn ([calendar] (intraday/current-close-dt calendar n unit))
                     ([calendar dt] (intraday/current-close-dt calendar n unit dt)))

   ; open
   :next-open      (fn ([calendar] (intraday/next-open-dt calendar n unit))
                     ([calendar dt] (intraday/next-open-dt calendar n unit dt)))

   :prior-open     (fn ([calendar] (intraday/prior-open-dt calendar n unit))
                     ([calendar dt] (intraday/prior-open-dt calendar n unit dt)))

   :current-open   (fn ([calendar] (intraday/current-open-dt calendar n unit))
                     ([calendar dt] (intraday/current-open-dt calendar n unit dt)))

   ; duration
   :duration       (t/divide (t/new-duration n unit) (t/new-duration 1 :seconds))})

(def intervals
  {:Y   {:next-close    year/next-close
         :prior-close   year/prior-close
         :current-close year/current-close}

   :M   {:next-close    month/next-close
         :prior-close   month/prior-close
         :current-close month/current-close}

   :W   {:next-close    week/next-close
         :prior-close   week/prior-close
         :current-close week/current-close}

   :d   {:next-close    day/next-close-dt
         :prior-close   day/prior-close-dt
         :current-close day/current-close}

   ; hour
   :h   (gen-intraday-step-fn 1 :hours)
   :h2  (gen-intraday-step-fn 2 :hours)
   :h3  (gen-intraday-step-fn 3 :hours)
   :h4  (gen-intraday-step-fn 4 :hours)
   :h6  (gen-intraday-step-fn 6 :hours)
   :h8  (gen-intraday-step-fn 8 :hours)
   :h12 (gen-intraday-step-fn 12 :hours)

   ; minute
   :m   (gen-intraday-step-fn 1 :minutes)
   :m2  (gen-intraday-step-fn 2 :minutes)
   :m3  (gen-intraday-step-fn 3 :minutes)
   :m4  (gen-intraday-step-fn 4 :minutes)
   :m5  (gen-intraday-step-fn 5 :minutes)
   :m6  (gen-intraday-step-fn 6 :minutes)
   :m10 (gen-intraday-step-fn 10 :minutes)
   :m12 (gen-intraday-step-fn 12 :minutes)
   :m15 (gen-intraday-step-fn 15 :minutes)
   :m20 (gen-intraday-step-fn 20 :minutes)
   :m30 (gen-intraday-step-fn 30 :minutes)})

(defn interval-exists? [interval-kw]
  (contains? intervals interval-kw))

(defn get-interval-list []
  (keys intervals))

(comment
  (now-in-zone "Europe/Paris")
  (now-in-zone "America/New_York")

  (-> (now-in-zone "America/New_York")
      (t/with  :minute-of-hour  0)
      (t/with :second-of-minute 0)
      (t/with :nano-of-second 0))

  (require '[cljc.java-time.zone-id :refer [get-available-zone-ids]])
  (->> (get-available-zone-ids)
       (map str)
       (sort)
       println)

  (:h intervals)
  (gen-current-close align-d day/next-close)

  (def day (:day intervals))
  day
  (require '[ta.calendar.calendars :refer [calendars]])
  (def us (:us calendars))
  us
  (def hour (:h intervals))
  hour

  (def next-close-day (:next-close day))
  (next-close-day us (now-calendar us))
  (def next-close-hour (:next-close hour))
  (next-close-hour us (now-calendar us))

  (def current-close-day (:current-close day))
  (current-close-day us)

 ; 
  )
