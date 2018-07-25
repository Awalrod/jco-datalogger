// gcdcNode constructor

function gcdcNode(nodeIndex){
    this.channels = ['x','y','z','mag']
    this.limit = 2000 //10 seconds of data at 200Hz
    this.nodeIndex = nodeIndex
    for(i in this.channels){
        chName = this.channels[i]
        this[chName] = new gcdcChannel("n"+this.nodeIndex+"c"+chName)
    }
    
    
    this.appendData = (sensorData,filter)=>{
    /*
     * sensorData is a simple object with x,y,z ints. JSON message should be in format already
     * filter is a function that accepts sensorData and a gcdcNode and returns sensorData
     * filter is optional
     */
        //check if magnitude is provided
        if(!sensorData.hasOwnProperty('mag')){
            sensorData['mag'] = Math.round(Math.sqrt(sensorData.x**2+sensorData.y**2+sensorData.z**2))
        }
     
        if(typeof filter !== 'undefined'){
            sensorData = filter(sensorData,this)
        }
        for(chName in sensorData){
            channel = this[chName]
            if(channel.data.length>this.limit){
                channel.data.shift()
            }
            channel.data.push([Date.now(),sensorData[chName]])
        }
    }
}

//gcdcChannelConstructor
function gcdcChannel(name){
    this.z0=0
    this.data=[]
    this.name=name
}

function iirFilter(sensorData, node){
    alpha = .98
    newSensorData = {}
    for(chName in sensorData){
        channel = node[chName]
        dataIn = sensorData[chName]
        z0 = channel.z0
        z1 = alpha*z0+(1-alpha)*dataIn
        channel.z0 = z1
        newSensorData[chName] = dataIn-z1
    }
    return newSensorData
}

function dummyFilter(sensorData, node){
    return sensorData //leaves data unchanged
}