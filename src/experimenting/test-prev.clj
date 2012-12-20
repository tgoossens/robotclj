(ns robotnxt.test)



;lightsensor 

(defn virtual-lightsensor [virtual]
  {:read (fn [robot] 0)} )

(defn remote-lightsensor [dispatch-fn]
	{:read (dispatch-fn robot :lightsensor)})




;pilot 

(defn virtual-commander [virtual]
  {:travel (fn [distance direction] direction)
   :drive (fn [direction] direction)
   :rotate (fn [rotation] rotation)
   :turn (fn [angle rotation] rotation)})

(defn remote-commander [dispatch-fn]
   {
   :travel (fn [distance direction] direction)
   :drive (fn [direction] direction)
   :rotate (fn [rotation] rotation)
   :turn (fn [angle rotation] rotation)
   
   :whiteline (fn [])
   :center (fn [])
   :explore (fn [])})



;EXECUTOR SHOULD BE MERGED WITH REQUEST (Communicator? ) 
; because executor must also send back when its ready. 

;can pilot also be merged? or not?

; Is tis too complex? can it be simpler?

;executor 

;(defn virtual-executor [virtual]
; {:whiteline (fn [] )
; 	:center (fn [])
; 	:explore (fn [])});

;(defn remote-executor [dispatch-fn]
; {:whiteline (fn [] )
; 	:center (fn [])
; 	:explore (fn [])})


;requesthandler: handling status requests

;THIS IS TOO 	 COMPLEX. 
; There should be a function to read the sensors instead and the function will - after a certain time -
; fail or return value.
; a client can choose to to this in a different thread and wrap it into a promise. 
; 

(defn virtual-requesthandler [virtual]
	"Returns a function that expects a promise (receiver)"
 {:color (fn [receiver] (deliver receiver 0))
 	:distance (fn [receiver] (deliver receiver 255))
 	:pose (fn [receiver] (deliver receiver {:position [0 0] :angle 0}))})


(defn remote-requesthandler [dispatch-fn]
 {:color (fn [receiver])
 	:distance (fn [receiver])
 	:pose (fn [receiver] )})


;controll a robot
;merge executor with pilot
;maybe this can be a request as well. and the receiver gets notified when the action is complete? 


(defn command [robot action]
	(-> robot :pilot action))

(defn drive [robot direction]
	((-> robot :pilot :travel) direction))

(defn rotate [robot rotation]
	((-> robot :pilot :rotate) direction))


(defn travel [robot distance direction]
	((-> robot :pilot :travel) distance direction))

(defn turn [robot angle rotation]
	((-> robot :pilot :turn) angle rotation))

(defn whiteline [robot]
	((-> robot :executor :whiteline)))


;request something from a robot. Requests must be answered

(defn request [robot subject]
 (fn [receiver]
  ((-> robot :request-handler subject) receiver)))

(defn color [robot]
	(request robot :color))

(defn distance [robot]
	(request robot :distance))

(defn pose [robot]
	(request robot :pose))




(defn robot [lightsensor pilot executor request-handler]
	"A robot is made up of components"
	{:lightsensor lightsensor
	 :pilot pilot
	 :executor executor
	 :request-handler request-handler})


(defn virtual-robot []
	(robot (virtual-lightsensor 0) (virtual-pilot 0) (virtual-executor 0) (virtual-requesthandler 0))) 


;some notes
; 
;The robot is made up of components
;The virtual that is given to each component containts information.
;The virtual knows the position, orientation, speed (?), ;
;
;The virtual can be sent commands and requests. 
;



(comment 
(def v (virtual-robot))
(drive v :forward)
(travel v 20 :forward)
(whiteline v)
	)

