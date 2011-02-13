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

(defn answer-filter [map]
  (let [ title (:title (:attrs map))]
    (or  (= "Result" title)
         (= "Decimal approximation" title)
         (= "Solution" title))))

(defn select-answer [resource]
  (filter answer-filter (select resource [:pod])))

(defn get-answer [question]
  (first  (second  (map :content (select (html-resource (get-content (query question))) [:plaintext])))))
