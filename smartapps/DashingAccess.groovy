/**
 *  Test
 *
 *  Copyright 2015 Alfredo Fiebig
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Test",
    namespace: "cl.afiebig",
    author: "Alfredo Fiebig",
    description: "Test",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "Afiebig Test", displayLink: ""])


preferences {

/*  page(name:"uno",title:"Sensores",nextPage:"dos") {
        section("Notify on when cofee windows Open:") {
            input "cofeeWindow", "capability.contactSensor", required: false, title: "Where?"
        }
        section("Notify on when motion detected:") {
            input "motionSensor", "capability.motionSensor", required: false, title: "Where?"
        }
    }

    page(name:"dos",title:"Notificaciones",nextPage:"tres"){
    /*  section("Send Notifications?") {
            input("recipients", "contact", title: "Send notifications to"){
                input "phone", "phone", title: "This are SMS notifications",
                    description: "Phone Number", required: false
            }
        }*/
/*        section("Send Push Notification?") {
           input "sendPush", "bool", required: false,
              title: "Send Push Notification when Opened?"
        }
    }
    
    page(name:"tres",title:"Configuracion Usuario",install:true,uninstall:true){
        section("Include a parameter by user") {
            input "minutes", "number", required: false, title: "Minutes?"
        }
        section ("Allow external service to control these things...") {
            input "temperature", "capability.temperatureMeasurement", multiple: true, required: false
        }
    }
    */
    section ("Allow external service to control these things...") {
        input "temperature", "capability.temperatureMeasurement", multiple: true, 
        required: false, title: "Temperatura:"
    }
}

mappings {
    path("/temperature") {
        action: [
            GET: "listSensors",
        ]
    }
    path("/temperature/:id") {
        action: [
            GET: "getSensorData"
        ]
    }
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSensors(){
    def resp = []
    temperature.each {
      resp << [name: it.displayName, value: it.currentValue("temperature")]
      log.debug "Dispositivo: ${it.displayName}, Temperature: ${it.currentValue("temperature")}"
    }
    return resp
}

def getSensorData(){
    // use the built-in request object to get the command parameter
    def id = params.id
    
    if(id.isNumber() && settings.temperature.size()  <= id){
        int i = 0
        def resp = []
  //      log.debug "Settings: ${settings.temperature}"
  //      log.debug "Settings: ${settings.temperature.size()}"
        temperature.each{
            if(id.equalsIgnoreCase(String.valueOf(i))){
                resp << [name: it.displayName, value: it.currentValue("temperature")]
                log.debug "Dispositivo: ${it.displayName},Temperature: ${it.currentValue("temperature")}"
                i = i+1
            } else {
                i= i+1
            }
        }
        return resp
    } else{
        log.debug "Id incorrecto"
        httpError(501, "$command is not a valid command for all switches specified")    
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
//    TODO: subscribe to attributes, devices, locations, etc.
//    subscribe(motionSensor, "motion.active", motionDetectedHandler)
//    subscribe(motionSensor, "motion.inactive", motionStopedHandler)
//    subscribe(cofeeWindow, "contact.open", windowOpenHandler)
//    subscribe(cofeeWindow, "contact.closed", windowClosedHandler)
}

// TODO: implement event handlers
def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    
    // check that contact book is enabled and recipients selected
    if (location.contactBookEnabled && recipients) {
        //Send notification to contact, use [event: false] as 3º param, to aboid 
        //the message to apear in the notification feed of the app
        sendNotificationToContacts("something you care about!", recipients)
    } else if (phone) { // check that the user did select a phone number
        sendSms(phone, "something you care about!")
    }
}

def motionStopedHandler(evt) {
    log.debug "motionStopedHandler called: $evt"
  //  runIn(60 * minutes, checkMotion)
}

def checkMotion() {
    log.debug "Cheking Motion"
    
    // get the current state object for the motion sensor
    def motionState = motionSensor.currentState("motion")

    if (motionState.value == "inactive") {
            // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutes

            if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            //theswitch.off()
            } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
            // Motion active; just log it and do nothing
            log.debug "Motion is active, do nothing and wait for inactive"
    }
}

def windowOpenHandler(evt){
    log.debug "windowsOpenHandler called: $evt"
    if (sendPush) {
        sendPush("The ${cofeeWindow.displayName} is open!")
    }
}

def windowClosedHandler(evt){
    log.debug "windowsClosedHanlder called: $evt"
     if (sendPush) {
        sendPush("The ${cofeeWindow.displayName} is closed!")
    }
}