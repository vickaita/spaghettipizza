(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   [dommy.macros :refer [node]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.dom.forms :as forms]
            [goog.events :as evt]
            [goog.net.XhrIo :as xhr]
            [goog.net.WebSocket]
            [cljs.core.async :refer [put! chan <! map<]]
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

(defn normalize-point
  "Convert an event into a point."
  [e]
  ;(.preventDefault e)
  ;; TODO: finding an offset parent is a little more complicated than this:
  ;; what if the parent element is svg?
  (let [elem (.-currentTarget e)
        offset-parent (if-not (= "svg" (.toLowerCase (.-tagName elem)))
                        elem (.-parentElement elem))
        left (.-offsetLeft offset-parent)
        top (.-offsetTop offset-parent)]
    (case (.-type e)
      ("touchstart" "touchmove") (let [t (-> e .getBrowserEvent .-touches (aget 0))]
                                   [(- (.-pageX t) left) (- (.-pageY t) top)])
      "touchend" nil
      (let [b (.getBrowserEvent e)]
        [(- (.-pageX b) left) (- (.-pageY b) top)]))))

(def current-noodle (atom nil))
(def current-tool (atom :spaghetti))

;(defn loggy [message obj]
;  (dom/setTextContent (dom/getElement "log") (str message pt)))

;(defn enable-spaghetti-drawing
;  "The most important function in all of spaghetti pizza!
;  Manages the drawing events that are monitored."
;  [svg-elem]
;  (let [e->pt (partial normalize-point svg-elem)
;        touchstart (map< e->pt (event-channel "touchstart" svg-elem))
;        touchend (map< e->pt (event-channel "touchend" svg-elem))
;        touchmove (map< e->pt (event-channel "touchmove" svg-elem))
;        touchcancel (map< e->pt (event-channel "touchcancel" svg-elem))
;        down (map< e->pt (event-channel "mousedown" svg-elem))
;        move (map< e->pt (event-channel "mousemove" svg-elem))
;        up (map< e->pt (event-channel "mouseup" js/document))]
;    (go (while true
;          (alt! [down touchstart]
;                ([pt] (let [n (create-topping @current-tool pt)]
;                        (loggy "start" pt)
;                        (reset! current-noodle n)
;                        (dom/append svg-elem (:element n))))
;
;                [move touchmove]
;                ([pt]
;                 (loggy "move" pt)
;                 (swap! current-noodle add-point! pt))
;
;                [up touchend]
;                ([pt] (do (loggy "end" pt)
;                          (reset! current-noodle nil)))
;
;                [touchcancel]
;                ([pt] (loggy "cancel" pt))
;                )))))

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
              (activate! (dom/getChildren toolbar) elem)))))))

(defn enable-registration
  [form]
  (evt/listen form "submit"
              (fn [e]
                (.preventDefault e)
                (let [req (ajax/request-form form)]
                  (forms/setDisabled form true)
                  (go (let [res (<! req)]
                        (.log js/console res)))))))

(defn connect-to-server
  []
  (let [ws (websocket "ws://ui:8080/ws")]
    (go (while true
          (.log js/console (<! ws))))
    #(put! ws %)))

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
                  (let [img-chan (svg/svg->img-chan svg-elem)]
                    (go (let [uri (<! img-chan)]
                          ;"http://api.spaghettipizza.us/pizza/" 
                          (xhr/send "/pizza/"
                                    (fn [resp] (.log js/console resp))
                                    "POST"
                                    uri)
                          (dom/removeChildren pizza-container)
                          (dom/append pizza-container (node [:img {:src uri}]))
                          (cls/remove modal "hidden"))))))))

(defn -main
  []
  (let [svg-elem (dom/getElement "main-svg")
        #_send-message #_(connect-to-server)]
    (evt/listen (dom/getElement "clean") "click"
                #(doto svg-elem
                   dom/removeChildren
                   pzz/draw-pizza))
    (pzz/draw-pizza svg-elem)
    (enable-spaghetti-drawing svg-elem)
    (enable-tool-selection (dom/getElement "toolbar"))
    (enable-photo-button (dom/getElement "photo") svg-elem)))

(evt/listen js/document "DOMContentLoaded" -main)
#_(repl/connect "http://ui:9000/repl")
