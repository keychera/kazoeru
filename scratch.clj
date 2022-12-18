#!/usr/bin/env bb

(ns user
  (:require [clojure.test :refer [deftest is run-tests testing]]))

(def order 10000)

(defn break-digit [inp]
  (loop [num inp
         acc []]
    (if (< (/ num order) 1)
      (conj acc {:log10e4 (count acc) :num num})
      (let [first-order (rem num order)
            remaining-order (/ (- num first-order) order)
            accumulated (conj acc {:log10e4 (count acc) :num first-order})] 
        (recur remaining-order accumulated)))))

(defn arabic->japanese [num]
  (-> num Long. break-digit))

(def input "4200310020")

(def expected [{:log10e4 0
                :num 20}
               {:log10e4 1
                :num 31}
               {:log10e4 2
                :num 42}])

(deftest name-test
  (testing "converter"
    (is (= (arabic->japanese input) expected))))

(run-tests 'user)

