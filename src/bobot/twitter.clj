(ns bobot.twitter
  (:require [clj-http.client :as client
             clojure.data.json :as json])
  (:import  [ java.util.concurrent Executors ScheduledExecutorService TimeUnit]))


(def since-map (agent {}))

(defn set-last-result! [query id]
  (send since-map assoc (keyword query) id))

(defn- search-since [q since-id]
  (println(str  "http://search.twitter.com/search.json?q=" q "&since_id=" since-id))
  (json/read-json
   (:body  (client/get (str  "http://search.twitter.com/search.json?q=" q "&since_id=" since-id)  {:accept :json}))))

(defn search [q]
  (let [result  (search-since q (( keyword q) @since-map))]
    (set-last-result! q (:max_id result))
    result))



(defn format-row [row]
  (str (:from_user row) ": " (:text row)))

(defn format-results [result]
  (filter #(not (nil? %)) (map  format-row  (:results  result))))



(def scheduler  (Executors/newScheduledThreadPool 1))


(defn repeat [task seconds]
  (.scheduleAtFixedRate scheduler task 0 seconds TimeUnit/SECONDS))
