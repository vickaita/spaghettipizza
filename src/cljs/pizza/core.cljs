(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   [dommy.macros :refer [node]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.dom.forms :as forms]
            [goog.events :as evt]
            [goog.net.XhrIo :as xhr]
            [goog.net.WebSocket]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [dommy.core]
            [vickaita.channels :refer [event websocket]]
            [vickaita.console :refer [log]]
            [pizza.ajax :as ajax]
            [pizza.svg :as svg]
            [pizza.pizza :as pzz]
            [pizza.spaghetti :refer [create-topping add-point!]]))

(defn event-channel
  [event-type element]
  (let [out (chan)]
    (evt/listen element event-type (fn [e] (put! out e)))
    out))

(defn xhr-channel
  [url method data]
  (let [out (chan)]
    (xhr/send url (fn [res] (put! out res)) method data)
    out))

(defn adjust-easel-size
  [easel]
  (let [size (.getBoundingClientRect (dom/getElement "page"))
        side (min (.-width size) (.-height size))]
    (doto easel
      (.setAttribute "width" side)
      (.setAttribute "height" side)
      (.setAttribute "viewport" (str "0 0 " side " " side)))))

(defn normalize-point
  "Convert an event into a point."
  [e]
  (let [elem (.-currentTarget e)
        offset (.getBoundingClientRect elem)
        left (.-left offset)
        top (.-top offset)]
    (case (.-type e)
      ("touchstart" "touchmove") (let [t (-> e .getBrowserEvent .-touches (aget 0))]
                                   [(- (.-pageX t) left) (- (.-pageY t) top)])
      "touchend" nil
      (let [b (.getBrowserEvent e)]
        [(- (.-pageX b) left) (- (.-pageY b) top)]))))

(def current-noodle (atom nil))
(def current-tool (atom :spaghetti))

(defn- start-noodle
  [e]
  (.preventDefault e)
  (let [pt (normalize-point e)
        n (create-topping @current-tool pt)]
    (reset! current-noodle n)
    (dom/append (.-currentTarget e) (:element n))))

(defn- move-noodle
  [e]
  (.preventDefault e)
  (let [pt (normalize-point e)]
    (swap! current-noodle add-point! pt)))

(defn- end-noodle
  [e]
  (.preventDefault e)
  (reset! current-noodle nil))

(defn enable-spaghetti-drawing
  [svg-elem]
  (evt/listen svg-elem "mousedown" start-noodle)
  (evt/listen svg-elem "touchstart" start-noodle)
  (evt/listen svg-elem "mousemove" move-noodle)
  (evt/listen svg-elem "touchmove" move-noodle)
  (evt/listen js/document "mouseup" end-noodle)
  (evt/listen svg-elem "touchup" end-noodle))

(defn- activate!
  "Iterate through all the elements in node-list removing class-name except for
  node which will have class-name added."
  ([node-list node] (activate! node-list node "active"))
  ([node-list node class-name]
  (dotimes [i (alength node-list)]
    (let [n (aget node-list i)]
      (if (= node n)
        (cls/add node class-name)
        (cls/remove n class-name))))))

(defn enable-tool-selection
  [toolbar]
  (let [clicks (map< #(.-target %) (event-channel "click" toolbar))]
    (go (while true
          (let [elem (<! clicks)
                tool (keyword (.getAttribute elem "data-tool"))]
            (when tool
              (reset! current-tool tool)
              (js/ga "send" "event" "tool" "select" (name tool))
              (activate! (dom/getElementsByClass "tool" toolbar) elem)))))))

(defn enable-photo-button
  [button svg-elem]
  (let [body (.-body js/document)
        ;; TODO: consider using one of the goog.ui classes such as Dialog or
        ;; ModalPopup here instead of this homegrown solution.
        modal (node [:div.modal-overlay.hidden
                     [:div.modal-wrap
                      [:div.modal-content
                       [:p "Share this with your friends!"]
                       [:div.pizza-container]]]])
        pizza-container (dom/getElementByClass "pizza-container" modal)]
    (dom/append body modal)
    (evt/listen modal "click" (fn [e] (cls/add modal "hidden")))
    (evt/listen (dom/getElementByClass "modal-content" modal) "click"
                (fn [e] (.stopPropagation e)))
    (evt/listen button "click"
                (fn [e]
                  (.preventDefault e)
                  (go (let [[uri blob] (<! (svg/svg->img-chan svg-elem))
                            ;"http://api.spaghettipizza.us/pizza/"
                            data (doto (js/FormData.) (.append "data" blob))
                            resp (<! (xhr-channel "/pizza/" "POST" data))]
                        (.log js/console resp)
                        (dom/removeChildren pizza-container)
                        (dom/append pizza-container (node [:img {:src uri}]))
                        (cls/remove modal "hidden")))))))

(defn -main
  []
  (let [svg-elem (dom/getElement "main-svg")
        #_send-message #_(connect-to-server)]

    #_(adjust-easel-size svg-elem)
    (evt/listen (dom/getElement "clean") "click"
                #(doto svg-elem
                   dom/removeChildren
                   pzz/draw-pizza))
    (pzz/draw-pizza svg-elem)

    (evt/listen (dom/getElement "menu-control") "click"
                #(cls/toggle (.-body js/document) "hide-toolbar"))

    (enable-spaghetti-drawing svg-elem)
    (enable-tool-selection (dom/getElement "toolbar"))
    (enable-photo-button (dom/getElement "photo") svg-elem)))

(evt/listen js/document "DOMContentLoaded" -main)
#_(repl/connect "http://ui:9000/repl")
