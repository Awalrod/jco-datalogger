//streamGraph constructor

function StreamGraph(width,height,id){
    this.min=-300000
    this.max= 300000
    this.width=width
    this.height=height
    this.limit = 2000
   
    this.Node={
        x:{
            vis: true,
            z0: 0,//for iir filter
            data: d3.range(this.limit).map(function() {
                return [Date.now(),0] //[time,val]
            })
        },
        y:{
            vis: true,
            z0: 0,
            data: d3.range(this.limit).map(function() {
                return [Date.now(),0]
            })
        },
        z:{
            vis:true,
            z0: 0,
            data: d3.range(this.limit).map(function() {
                return [Date.now(),0]
            })
        },
        mag:{
            vis:false,
            z0:0,
            data: d3.range(this.limit).map(function() {
                return [Date.now(),0]
            }) 
        }
    }
    this.x = d3.time.scale()
        .domain([Date.now()-10000, Date.now()])
        .range([0, width])

    this.y = d3.scale.linear()
        .domain([this.min, this.max])
        .range([height, 0])

    this.line = d3.svg.line()
        .interpolate('linear')
        .x((d, i)=> {
            return this.x(d[0])
        })
        .y((d)=> {
            return this.y(d[1])
        })

    this.svg = d3.select(id).append('svg')
            .attr('class', 'chart')
            .attr('width', width+20)
            .attr('height', height+20)

    this.xaxis = this.svg.append('g')
            .attr('class', 'x axis')
            .attr('transform', 'translate(0,' + height + ')')
            .call(this.x.axis = d3.svg.axis().scale(this.x).orient('bottom'))
    this.yaxis = this.svg.append('g')
            .attr('class','y axis')
            .call(this.y.axis = d3.svg.axis().scale(this.y).orient('right'))


    this.h1=this.min
    this.h2=this.max
    
    this.drawGuides = () =>{
        this.h1Container = this.svg.append('g')
        this.h2Container = this.svg.append('g')
        this.h1Text = this.h1Container.append('text')
            .datum(this.h1)
            .attr('transform',(d)=>{
                return 'translate('+(this.width-25)+','+(this.y(d)-3)+')'
            })
            .text((d) => {
                return d
            })
            .attr("font-size","8px")
            .attr("font-family","monospace")
        this.h1Line=this.h1Container.append('line')
            .datum(this.h1)
            .attr('class','horizontalGuide')
            .attr('x1',0)
            .attr('x2',this.width)
            .attr('y1',0)
            .attr('y2',0)
            .attr('transform', (d)=>{
                return 'translate(0,'+this.y(d)+')'
            })            
        this.h2Text = this.h2Container.append('text')  
            .datum(this.h2)
            .attr('transform',(d)=>{
                return 'translate('+(this.width-25)+','+(this.y(d)+8)+')'
            })
            .text((d) => {
                return d
            })
            .attr("font-size","8px")
            .attr("font-family","monospace")
            
        this.h2Line=this.h2Container.append('line')
            .datum(this.h2)
            .attr('class','horizontalGuide')
            .attr('x1',0)
            .attr('x2',this.width)
            .attr('y1',0)
            .attr('y2',0)
            .attr('transform', (d)=>{
                return 'translate(0,'+this.y(d)+')'
            })
    }
    this.drawGuides() //first draw
    
    this.redrawGuides = () =>{
        this.h1Container.remove()
        this.h2Container.remove()
        this.drawGuides()    
    }
    
    this.setGuides = (y1,y2)=>{
        this.h1 = y1<y2? y1:y2
        this.h2 = y1>y2? y1:y2
        this.redrawGuides()
    }
    this.updateBounds= (lower,upper)=>{
        this.y.domain([lower,upper])
        this.yaxis.transition()
            .duration(0)
            .ease('linear')
            .call(this.y.axis)
        this.min = lower
        this.max = upper
        this.redrawGuides()
        this.updateGraph()
    }
            
    this.paths = this.svg.append('g')

    for (var name in this.Node) {
            this.Node[name].path = this.paths.append('path')
                    .data([this.Node[name].data])
                    .attr('class', name + ' group')
                    .attr('data-legend',function(d){return name.slice()})
//                    .style('stroke', this.Node[name].color)
    }
    
    this.legend = this.svg.append("g")
      .attr("class","legend")
      .attr("transform","translate(80,20)")
      .attr("data-style-padding",5)
      .style("font-size","12px")
      .style("font-color","black")
      .style("fill","white")
      .style("stroke","black")
      .call(d3.legend)


    
    this.updateData = (event) => { 
        now = new Date(Date.now())
        var sensorData = JSON.parse(event.data)
        sumOfSquares = 0
        for(name in sensorData){
            d = sensorData[name]
            sumOfSquares += d**2
        }
        mag = Math.round(Math.sqrt(sumOfSquares))
        sensorData["mag"]=mag
        
        sensorData = this.filter(sensorData)
        for( var name in this.Node){
            group = this.Node[name]
            group.data.push([now,sensorData[name]])
            if(group.data.length>this.limit){
                group.data.shift()
            }
        }               
        this.x.domain([now-(10*1000),now])
    }
    
    this.IIR = false;
    this.alpha = .9
    this.filter = (sensorData) =>{
        if(this.IIR){
            newSensorData={}//empty object
            for(var name in this.Node){
                group = this.Node[name]
                dataIn = sensorData[name]
                z0 = group.z0
                z1 = this.alpha*z0+(1-this.alpha)*dataIn
                newSensorData[name] = dataIn-z1
                group.z0=z1
            }
            return newSensorData
        }//add more as needed
        else{
            return sensorData
        }
    }
    
    this.clearFilter = () =>{
        this.IIR = false
    }
    
    
    this.updateGraph = () => {
        for(var name in this.Node){
            if(this.Node[name].vis){
                this.Node[name].path.attr('display','')
                this.Node[name].path.attr('d',this.line);
            }else{
                this.Node[name].path.attr('display','none')
            }
        }
        this.xaxis.transition()
            .duration(0)
            .ease('linear')
            .call(this.x.axis)
    }
    

}


(function() {
d3.legend = function(g) {
  g.each(function() {
    var g= d3.select(this),
        items = {},
        svg = d3.select(g.property("nearestViewportElement")),
        legendPadding = g.attr("data-style-padding") || 5,
        lb = g.selectAll(".legend-box").data([true]),
        li = g.selectAll(".legend-items").data([true])

    lb.enter().append("rect").classed("legend-box",true)
    li.enter().append("g").classed("legend-items",true)

    svg.selectAll("[data-legend]").each(function() {
        var self = d3.select(this)
        items[self.attr("data-legend")] = {
          pos : self.attr("data-legend-pos") || this.getBBox().y,
          color : self.attr("data-legend-color") != undefined ? self.attr("data-legend-color") : self.style("fill") != 'none' ? self.style("fill") :self.style("stroke")
        }
      })

    items = d3.entries(items).sort(function(a,b) { return a.value.pos-b.value.pos})


    li.selectAll("text")
        .data(items,function(d) { return d.key})
        .call(function(d) { d.enter().append("text")})
        .call(function(d) { d.exit().remove()})
        .attr("y",function(d,i) { return i+"em"})
        .attr("x","1em")
        .text(function(d) { ;return d.key})

    li.selectAll("circle")
        .data(items,function(d) { return d.key})
        .call(function(d) { d.enter().append("circle")})
        .call(function(d) { d.exit().remove()})
        .attr("cy",function(d,i) { return i-0.25+"em"})
        .attr("cx",0)
        .attr("r","0.4em")
        .style("fill",function(d) { return d.value.color})  

    // Reposition and resize the box
    var lbbox = li[0][0].getBBox()  
    lb.attr("x",(lbbox.x-legendPadding))
        .attr("y",(lbbox.y-legendPadding))
        .attr("height",(lbbox.height+2*legendPadding))
        .attr("width",(lbbox.width+2*legendPadding))
  })
  return g
}
})()

