(ns om-css.css
  (:require [cljs.tagged-literals]
            [clojure.string :as str]
            [com.rpl.specter :as sp]
            [om.next :as om]
            [garden.core :as g]
            [cljs.core]
            [om-css.core :as oc]))

(defprotocol CSS
  (local-rules [this] "Specifies the component's local CSS rules")
  (children [this] "Specifies the direct children that implement the CSS or Global protocol"))

(defprotocol Global
  (global-rules [this] "Specifies the component's global CSS rules"))

#?(:clj (defn implements-protocol?
          [x protocol protocol-key]
          (if (fn? x)
            (some? (-> x meta protocol-key))
            (extends? protocol (class x)))))

(defn CSS?
  [x]
  #?(:clj (implements-protocol? x CSS :local-rules)
     :cljs (implements? CSS x)))

(defn Global?
  [x]
  #?(:clj (implements-protocol? x Global :global-rules)
     :cljs (implements? Global x)))

(defn get-global-rules
  [component]
  (if (Global? component)
    #?(:clj ((:global-rules (meta component)) component)
       :cljs (global-rules component))
    []))

(defn get-local-rules
  [component]
  (if (CSS? component)
    #?(:clj ((:local-rules (meta component)) component)
       :cljs (local-rules component))
    []))

(defn prefixed-keyword?
  [kw]
  (and (keyword? kw)
       (str/starts-with? (name kw) ".")))

(defn remove-prefix
  [kw]
  (keyword (subs (name kw) 1)))

(defn get-children
  [component]
  (if (CSS? component)
    #?(:clj ((:children (meta component)) component)
       :cljs (children component))
    []))

(defn get-nested-children
  [component]
  (let [direct-children (get-children component)]
    (if (empty? direct-children)
      []
      (concat direct-children (reduce #(concat %1 (get-nested-children %2)) [] direct-children)))))

(defn localize-css
  [component]
  (sp/transform (sp/walker prefixed-keyword?)
                #(let [nm (subs (name %) 1)]
                   (oc/local-kw component (keyword nm)))
                (get-local-rules component)))

(defn get-css-rules
  [component]
  (concat (localize-css component) (get-global-rules component)))

(defn get-css
  [component]
  (let [own-rules (get-css-rules component)
        nested-children (distinct (get-nested-children component))
        nested-children-rules (reduce #(into %1 (get-css-rules %2)) [] nested-children)]
    (concat own-rules nested-children-rules)))

(defn get-classnames
  [comp]
  (let [local-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-local-rules comp))))
        global-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-global-rules comp))))
        local-classnames (zipmap local-kws (map #(oc/local-class comp %) local-kws))
        global-classnames (zipmap global-kws (map name global-kws))]
    (merge local-classnames global-classnames)))

#?(:cljs
   (defn upsert-css
     "(Re)place the STYLE element with the provided ID on the document's DOM  with the co-located CSS of the specified component."
     [id root-component]
     (oc/remove-from-dom id)
     (let [style-ele (.createElement js/document "style")]
       (set! (.-innerHTML style-ele) (g/css (get-css root-component)))
       (.setAttribute style-ele "id" id)
       (.appendChild (.-body js/document) style-ele))))



