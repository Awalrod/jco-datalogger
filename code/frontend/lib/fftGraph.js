function FFTGraph(width, height, id){
    this.desiredSize = 2**9 //powers of 2 work best 2**9=512 2**10=1024
    this.difs = new Array(this.desiredSize/4) //rolling average to calculate sample rate
    //this.data = d3.range(1).map(function(){
    //    return [0,0]
    //})
    this.mag_list = []
    this.x = d3.scale.linear()
        .domain([0,100])
        .range([0,width])
    this.y = d3.scale.linear()
        .domain([0,160])
        .range([height,0])
    this.line = d3.svg.line()
        .x((d) =>{
            return this.x(d[0])
        })
        .y((d) =>{
            return this.y(20*Math.log10(d[1]))
        })
    this.svg = d3.select(id).append('svg')
        .attr('class','chart')
        .attr('width', width +50)
        .attr('height', height +100)
        .on('mousemove',()=>{
            var bisector = d3.bisector(function(d){ return d.x; }).left;
            var xCoord = d3.mouse(this.svg[0][0])[0]
            var virt = this.x.invert(xCoord)
            //Use this to implement crosshair
        })
    this.xaxis = this.svg.append('g')
        .attr('class','x axis')
        .attr('transform','translate(0,'+height+')')
        .call(this.x.axis=d3.svg.axis().scale(this.x).orient('top'))
    this.yaxis = this.svg.append('g')
        .attr('class','y axis')
        .call(this.y.axis = d3.svg.axis().scale(this.y).orient('right'))
    this.paths = this.svg.append('g')
    
    
    //this.path = this.paths.append('path')
    //    .data([this.data])
        //.style('stroke','red')
        //.style('fill','none') //Do it in css
    
    

    this.label = this.svg.append("text")
    this.lastDate = new Date(Date.now())//used for difs
    
    //Unlike with streamGraph, we're doing 'heavy' calculations, so we keep a channel list instead of 
    // recalculating the paths every update. Can't afford to fft at 200hz
    this.chList = []
    this.updateData = (chWithColor) =>{
        
        for(i in chWithColor){
            ch = chWithColor[i][0]
            color = chWithColor[i][1]
            path = this.findPathById('#'+ch.name)
            if(path==null){
                this.paths.append('path')
                    .style('stroke',color)
                    .attr('id',ch.name)
                    .attr('class','group')
                this.chList.push(ch)
            }
        }
    }
    this.findPathById = (pathId)=>{
        path = this.paths.select(pathId)
        if(path[0][0]===null){
            return null
        }else{
            return path
        }
    }
    this.removePath = (pathId)=>{
        path = this.findPathById(pathId)
        if(path != null){
            for(i in this.chList){
                if(path[0][0].id == this.chList[i].name){
                    this.chList.splice(i,1)
                }
            }
            path.remove()
        }
    }
    
    //This is when we actually calculate the fft
    this.updateGraph = () =>{
        f_axis = null
        empty_imag = new Array(this.desiredSize).fill(0)
        for(i in this.chList){
            ch = this.chList[i]
            if(ch.data.length >= this.desiredSize){
                dif = 0
                for(i in ch.data){
                    if(i>0){
                        t1 = ch.data[i][0] //timestamp
                        t0 = ch.data[i-1][0]
                        dif = ((dif*i-1)+Math.abs(t1-t0))/i //rolling average
                    }
                }
                sr = 1000/dif
                if(f_axis == null){
                    f_axis = new Array(this.desiredSize/2).fill(0).map((d,i)=>{
                        return i*(sr/2)/(this.desiredSize/2)
                    })
                }
                ch_fft = ch.data.map((d)=>{
                    return d[1]
                })
                
                ch_fft = ch_fft.slice(-this.desiredSize)//make sure no reference to original and only use right amount 
                ch_imag = empty_imag.slice()
                
                transform(ch_fft, ch_imag) // transform is located in fft.js
                ch_mod = ch_fft.map((d,i)=>{
                    return Math.sqrt(d**2+ch_imag[i]**2)
                })
                ch_mod_half = ch_mod.slice(0,ch_mod.length/2)
                ch_zipped = f_axis.map((d,i)=>{
                    return [d,ch_mod_half[i]]
                })
                this.x.domain([0,f_axis[f_axis.length-1]])
                path = this.paths.select('#'+ch.name)
                    .data([ch_zipped])
                    .attr('d',this.line)
                this.xaxis.transition()
                    .duration(0)
                    .ease('linear')
                    
            }
        }   
    }
    
    /*this.updateData = (event) =>{
        now = new Date(Date.now())
        dif = now-this.lastDate
        this.lastDate = now
        this.difs.push(dif)
        this.difs.shift()
        sensorData= JSON.parse(event.data)
        var mag = Math.sqrt(sensorData.x**2+sensorData.y**2+sensorData.z**2)
        this.mag_list.push(mag)
        if(this.mag_list.length==this.desiredSize){
            empty_imag = new Array(this.desiredSize).fill(0)

            sr = 0
            for(var d in this.difs){
                sr = sr + this.difs[d]
            }
            sr = sr/this.difs.length
            sr = 1000/sr
            f_axis = new Array(this.desiredSize/2).fill(0).map((d,i)=>{
                return i*(sr/2)/(this.desiredSize/2)
            })
            
            var mag_fft = this.mag_list.slice() //make a copy
            var mag_imag = empty_imag.slice()
            
            transform(mag_fft,mag_imag)
            var mag_mod = mag_fft.map(function(d,i){
                return(Math.sqrt(d**2+mag_imag[i]**2))
            })
            var mag_mod_half = mag_mod.slice(0,mag_mod.length/2)
            
            mag_zipped = f_axis.map(function(d,i){
                return [d,mag_mod_half[i]]
            })
            this.path.data([mag_zipped])
            this.path.attr('d',this.line)
            this.x.domain([0,f_axis[f_axis.length-1]])
            this.xaxis.transition()
                .duration(0)
                .ease("linear")
                .call(this.x.axis)
                
            max = this.getMax(mag_mod_half.slice(1))
            this.drawMax([f_axis[max[1]+1],20*Math.log10(max[0])])//add 1 because we sliced the first index    
                
            this.mag_list = []        
        }
        
    }*/
    this.getMax = (numList) =>{
        var max = numList[0]
        i=0
        index=0
        while(i < numList.length){
            num = numList[i]
            if(max<num){
                max = num
                index = i
            }
            i++
        }
        return [max,index]
    }
    this.drawMax = (point) =>{
        this.label.remove()
        this.label = this.svg.append('text')
            .attr('transform','translate('+(this.x(point[0])+5)+','+(this.y(point[1])-5)+')')
            .text(Math.round(point[0])+'Hz')
            .attr("font-size", '9px')
            .attr('font-family','monospace')
            .style('stroke','black')
            .style('stroke-width','.5')
    }
}