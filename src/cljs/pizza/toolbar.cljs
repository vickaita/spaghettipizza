(ns pizza.toolbar
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [dommy.macros :refer [node]])
  (:require [cljs.core.async :refer [put! close! chan <! map<]]
            [cljs.reader :as reader]
            [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.events :as evt]
            [vickaita.channels :as ch]
            [pizza.svg :as svg]))

(def current-tool (atom :spaghetti))

;; Methods for showing and hiding the toolbar.
(defn show! [] (cls/add (.-body js/document) "show-toolbar"))
(defn hide! [] (cls/remove (.-body js/document) "show-toolbar"))
(defn toggle!  [] (cls/toggle (.-body js/document) "show-toolbar"))
(defn visible? [] (cls/contains (.-body js/document) "show-toolbar"))

(defn- activate-tool!
  "Iterate through all the elements in node-list removing class-name except for
  node which will have class-name added."
  [node-list node]
  (dotimes [i (alength node-list)]
    (let [n (aget node-list i)]
      (if (= node n)
        (cls/add node "active")
        (cls/remove n "active")))))

(defn enable-tool-selection!
  [toolbar]
  ;; Since dom/getElementsByClass returns a NodeList which is a "live"
  ;; collection we don't need to update it later.
  (let [tools (dom/getElementsByClass "tool" toolbar)
        clicks (map< #(.-target %) (ch/events "click" toolbar))]
    (go-loop [elem (<! clicks)]
             (when-let [tool (keyword (.getAttribute elem "data-tool"))]
               (reset! current-tool tool)
               (js/ga "send" "event" "tool" "select" (name tool))
               (activate-tool! tools elem)
               (hide!))
             (recur (<! clicks)))))

;(defn enable-tool-selection!
;  [toolbar]
;  (let [tools (dom/getElementByClass "tool" toolbar)]
;    (evt/listen
;      toolbar "click"
;      (fn [e]
;        (let [elem (.-target e)]
;          (when-let [tool (keyword (.getAttribute elem "data-tool"))]
;            (reset! current-tool tool)
;            (js/ga "send" "event" "tool" "select" (name tool))
;            (activate! tools elem)
;            (hide!))))))

(defn enable-save-button!
  [button svg-elem history]
  (evt/listen
    button "click"
    (fn [e]
      (.preventDefault e)
      (hide!)
      (go (let [api-url (if (= "spaghettipizza.us" (.-host (.-location js/document)))
                          "http://api.spaghettipizza.us/pizza/"
                          "/pizza/")
                blob (<! (svg/svg->img-chan svg-elem 612 612))
                data (doto (js/FormData.) (.append "data" blob))
                {fh :file-hash} (reader/read-string (<! (ch/xhr api-url "POST" data)))
                page-url (str #_(.-origin js/location) "?pizza=" fh)]
            (.setToken history page-url))))))
