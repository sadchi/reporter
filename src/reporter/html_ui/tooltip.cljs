(ns reporter.html-ui.tooltip
  (:require [reagent.core :as r]
            [reporter.tools :as tools]))


(def ^:private tooltip-atom (r/atom nil))

(defn tooltip []
  (fn []
    (let [{:keys [text x y align] :as data} @tooltip-atom
          class (str "tooltip__container--" align)]
      (when-not (empty? data)
        [:div.tooltip__container {:class class
                                  :style {:left x
                                          :top  y}}
         [:div.tooltip__content text]]
        ))))


(defn- offset [x]
  (loop [elem x
         acc [0 0]]
    ;(tools/log-obj "elem " elem)
    ;(tools/log-obj "acc " acc)
    (if (= "BODY" (.-tagName elem))
      acc
      (recur (.-offsetParent elem) [(+ (first acc) (.-offsetLeft elem)) (+ (second acc) (.-offsetTop elem))]))))

(defn show-tooltip [text align event]
  (let [target (.-target event)
        [left top] (offset target)
        ;_ (tools/log-obj "left: " left)
        ;_ (tools/log-obj "top: " top)
        width (.-offsetWidth target)
        height (.-offsetHeight target)
        ;_ (tools/log-obj "width: " width)
        ;_ (tools/log-obj "height: " height)
        align-final (if (contains? #{:left :right :center} align) align :center)

        x (condp = align-final
            :left left
            :right (+ left width)
            :center (+ left (/ width 2)))

        y (+ top height)
        ;_ (tools/log-obj "x: " x)
        ;_ (tools/log-obj "y: " y)
        ]

    (reset! tooltip-atom {:text  text
                          :x     x
                          :y     y
                          :align (name align-final)})))

(defn hide-tooltip []
  (reset! tooltip-atom nil))