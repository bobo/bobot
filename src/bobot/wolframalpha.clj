(ns bobot.wolframalpha
  (:use  [net.cgrand.enlive-html])
  (:require [clj-http.client :as client])
  (:import java.net.URL))


(defn get-content [url]
  (let [page (.openConnection (URL. url))]
    (.setRequestProperty page "User-agent" "irclj-bobot")
    (.getInputStream page)))

(def api-key "YOURKEY")

(defn query [q]  (str  "http://api.wolframalpha.com/v2/query?format=plaintext&input=" q  "&appid=" api-key))


(defn clean-up [map]
  {:title (-> map :attrs :title) :content (-> map :content second :content second :content first )})


(def good-pods [ "Solution" "Decimal approximation"])

(defn make-string [{content :content title :title}]
  (str " \"" title "\": " content " "))

(defn get-answer [question]
  (apply str (map make-string (take 3 (rest  (map clean-up  (select (html-resource (get-content (query question))) [:pod]))))))
)
