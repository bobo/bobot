(ns bobot.core
  (:use [irclj.core]
        [net.cgrand.enlive-html]
        [bobot.filelogging]
        [bobot.wolframalpha]
        [bobot.twitter]
        )
  (:require [clj-http.client :as client])
  (:import (java.net URL URLConnection URLEncoder)))


(defn url-encode
  "Wrapper around java.net.URLEncoder returning a (UTF-8) URL encoded
representation of text."
  [text]
  (URLEncoder/encode text "UTF-8"))

(defn make-url [address]
    (if (.startsWith address "http")
      (URL. address)
      (URL. (str "http://" address))))

(defn get-title [url]
  (-> url
      html-resource
      (select [:title])
      first :content first))


(defn google-it [query]
  (let [google (.openConnection (URL. (str "https://www.google.com/search?as_q=" query "&hl=sv")))]
    (.setRequestProperty google "User-agent" "irclj-bobot")
    (.getInputStream google)))

(defn select-from [query tag]
  (select (html-resource
           (google-it (url-encode query))) tag))

(defn do-search [query]
  (let [ url (->
              (select-from query [:a.l])
              first :attrs :href)]
    (str (-> url make-url get-title)
         " " url)))

(defn clean-html [page]
  (flatten (map #(if (map?  %) (clean-html (:content %)) %) page)))

(defn get-info [query]
  (apply str (->
              (select-from query [:div.s])
              first
              :content
              clean-html
              )))

(defn convert [query]
  (-> (select-from query [:h2.r])
        first :content first :content first str))



(defn search-command [irc channel message]
  (send-message irc channel
                (->  message
                     second
                     url-encode
                     do-search)))

(defn search-command-info [irc channel message]
  (send-message irc channel
                (->  message
                     second
                     url-encode
                     get-info)))





(defn say-title [irc channel message]
  (send-message irc channel
                (-> message
                    first
                    make-url
                    get-title)))

(defn cleanup [string]
  (->
   string
   (.replaceAll "Antal" "")
   (.replaceAll "anm.lda" "")))

(defn say-booked [irc channel]
  (send-message irc channel 
                (cleanup
                  (reduce  #(str %1 " \n " %2)
                           (filter #(.contains (str %) "Antal")
                                   (map first
                                        (map :content
                                             (-> "http://www.hazard.nu/nextlan_anm.asp" 
                                                 URL. 
                                                 html-resource 
                                                 (select [:p])))))))))

(defn say-convert [irc channel message]
  (send-message irc channel (convert (second  message))))


(def url-pattern  #"(http://[^ ]*)||(www.[^ ]*).*")

(defn say-is-prime [irc channel num]
  (send-message irc channel 
                (str  (.isProbablePrime (BigInteger. (second  num)) 42))))

(defn say-wolfram [irc channel question]
  (send-message irc channel (get-answer (url-encode (second  question)))))

;
(defn say-twitter [irc channel question]
  (let [result (format-results (search (second  question)))]
    (println (count result))
    (doall (map  #(send-message irc channel %) result))))


(defn repeater [irc channel question]
  (repeat #(say-twitter irc channel question) 10))

(defn on-message  [{:keys [channel message irc nick]}]
  (condp re-matches message
    url-pattern :>> (partial say-title irc channel)
    #"google:[ ]*(.*)" :>> (partial search-command irc channel)
    #"info:[ ]*(.*)" :>> (partial search-command-info irc channel)
    #"c:[ ]*(.*)" :>> (partial say-convert irc channel)
    #"prime: ([\d]*)" :>> (partial say-is-prime irc channel)
    #"wolfram:[ ]*(.*)" :>> (partial say-wolfram irc channel)
    #"twitter:[ ]*(.*)" :>> (partial say-twitter irc channel)
    #"rtwitter:[ ]*(.*)" :>> (partial repeater irc channel)
    #"bokade.*" (say-booked irc channel)
    nil))




