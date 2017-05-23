(ns om-css.core
  #?(:cljs (:require-macros om-css.core))
  (:require [cljs.tagged-literals]
            [clojure.string :as str]
            [com.rpl.specter :as sp]
            [garden.core :as g]
            [om.next :as om]
            [cljs.core]))

(defprotocol localCSS
  (local-css [this] "Specifies the component's local CSS"))

(defprotocol globalCSS
  (global-css [this] "Specifies the component's global CSS"))

(defprotocol childrenCSS
  (children-css [this] "Specifies the component's children that implement either localCSS or global CSS or both"))

(defn implements-protocol?
  [x protocol protocol-key]
  #?(:cljs {:tag boolean})
  [x]
  #?(:clj (if (fn? x)
            (some? (-> x meta protocol-key))
            (extends? protocol (class x)))
     :cljs (implements? protocol x)))

(defn localCSS?
  [x]
  (implements-protocol? x localCSS :local-css))

(defn globalCSS?
  [x]
  (implements-protocol? x globalCSS :global-css))

(defn childrenCSS?
  [x]
  (implements-protocol? x childrenCSS :children-css))


(defn cssify
  "Replaces slashes and dots with underscore."
  [str] (str/replace str #"[./]" "_"))

(defn fq-component [comp-class]
  #?(:clj (if (nil? (meta comp-class))
            (str/replace (.getName comp-class) #"[_]" "-")
            (str (:component-ns (meta comp-class)) "/" (:component-name (meta comp-class))))
     :cljs (pr-str comp-class)))

(defn local-kw
  "Generate a keyword for a localized CSS class for use in Garden CSS syntax as a localized component classname keyword."
  ([comp-class]
   (keyword (str "." (cssify (fq-component comp-class)))))
  ([comp-class nm]
   (keyword (str "." (cssify (fq-component comp-class)) "__" (name nm)))))

(defn local-class
  "Generates a string name of a localized CSS class. This function combines the fully-qualified name of the given class
     with the (optional) specified name."
  ([comp-class]
   (str (cssify (fq-component comp-class))))
  ([comp-class nm]
   (str (cssify (fq-component comp-class)) "__" (name nm))))

(defn localize-css
  [component css-rules]
  (sp/transform (sp/walker #(and (keyword? %) (str/starts-with? (name %) ".")))
                #(let [nm (subs (name %) 1)]
                   (local-kw component (keyword nm)))
                css-rules))

(defn call-css [component]
  #?(:clj ((:css (meta component)) component)
     :cljs (css component)))

(defn get-global-css [component]
  (if (globalCSS? component)
    #?(:clj ((:global-css (meta component)) component)
       :cljs (global-css component))
    []))

(defn get-local-css [component]
  (if (localCSS? component)
    #?(:clj ((:local-css (meta component)) component)
       :cljs (local-css component))
    []))

(defn get-children-css [component]
  (if (childrenCSS? component)
    #?(:clj ((:children-css (meta component)) component)
              :cljs (children-css component))
    []))

(defn get-css
  [component]
  (let [local-rules (localize-css component (get-local-css component))
        global-rules (get-global-css component)
        children-rules (if (childrenCSS? component) (apply #(get-css %) (get-children-css component)) [])]
    (concat local-rules global-rules children-rules)))

(defn prefixed-keyword?
  [kw]
  (and (keyword? kw)
       (str/starts-with? (name kw) ".")))

(defn remove-prefix
  [kw]
  (keyword (subs (name kw) 1)))

(defn get-classnames
  [comp]
  (let [local-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-local-css comp))))
        global-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-global-css comp))))
        local-classnames (zipmap local-kws (map #(local-class comp %) local-kws))
        global-classnames (zipmap global-kws (map name global-kws))]
    (merge local-classnames global-classnames)))

#?(:cljs
   (defn remove-from-dom "Remove the given element from the DOM by ID"
     [id]
     (if-let [old-element (.getElementById js/document id)]
       (let [parent (.-parentNode old-element)]
         (.removeChild parent old-element)))))

#?(:cljs
   (defn upsert-css
     "(Re)place the STYLE element with the provided ID on the document's DOM  with the co-located CSS of the specified component."
     [id root-component]
     (remove-from-dom id)
     (let [style-ele (.createElement js/document "style")]
       (set! (.-innerHTML style-ele) (g/css (get-css root-component)))
       (.setAttribute style-ele "id" id)
       (.appendChild (.-body js/document) style-ele))))
