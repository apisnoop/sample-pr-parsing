#!/usr/bin/env bb
(ns script
  (:require [clojure.data.xml :as xml]
             [cheshire.core :as json]))

(def junit (slurp "./junit_01.xml"))

(defn parse [s] (xml/parse (java.io.ByteArrayInputStream. (.getBytes s))))

;;;;
; xml->json
; ----------
; the next two functions are taken from https://rosario.io/2016/12/26/convert-xml-json-clojure
; the blog of Rosario Rascunà.  he made a library for this, but i couldn't require it in a babashka script.
; v thankful for his work!
;;;;
(defn different-keys? [content]
  (when content
    (let [dkeys (count (filter identity (distinct (map :tag content))))
          n (count content)]
      (= dkeys n))))

(defn xml->json [element]
  (cond
    (nil? element) nil
    (string? element) element
    (sequential? element) (if (> (count element) 1)
                            (if (different-keys? element)
                              (reduce into {} (map (partial xml->json ) element))
                              (map xml->json element))
                            (xml->json  (first element)))
    (and (map? element) (empty? element)) {}
    (map? element) (if (:attrs element)
                     {(:tag element) (xml->json (:content element))
                      (keyword (str (name (:tag element)) "Attrs")) (:attrs element)}
                     {(:tag element) (xml->json  (:content element))})
    :else nil))

; the json comes in with a number of empty strings that look like '\n     '
; lets filter them out
(def testsuite (filter #(not(string? %))
                       (:testsuite (xml->json (parse junit)))))

; all tests are in the testcaseAttrs key, under name
(def tests (map #(-> % :testcaseAttrs :name) testsuite))

(def conformance-tests (filter #(clojure.string/includes? % "[Conformance]") tests))

(spit "./junit-tests.json" (json/generate-string conformance-tests {:pretty true}))
(println "tests from junit.xml saved in junit-tests.json")
