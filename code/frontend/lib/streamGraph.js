//streamGraph constructor

function StreamGraph(width,height,id){
    this.min=-300000
    this.max= 300000
    this.width=width
    this.height=height
    this.limit = 2000
    
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
        
        tickMarks = this.yaxis.selectAll('.tick text')[0].map((d)=>{
            return Number(d.textContent.replace(",",""))    
        })
        
        range = this.max-this.min
        radius = range*.02
        for(i in tickMarks){
            if(Math.abs(this.h1-tickMarks[i]) < radius){
                this.h1 = tickMarks[i]
            }
            if(Math.abs(this.h2-tickMarks[i]) <radius){
                this.h2 = tickMarks[i]
            }
        }
        
        this.redrawGuides()
    }
    this.updateBounds= (lower,upper)=>{
        this.y.domain([lower,upper])
        this.yaxis.transition()
            .duration(0)
            .ease('linear')
            .call(this.y.axis)
        this.ticks = this.yaxis.selectAll('.tick')
        this.min = lower
        this.max = upper
        this.redrawGuides()
        this.updateGraph()
        
    }
            
    this.paths = this.svg.append('g')
    


    
    this.updateData = (chWithColor) => { 
    /*
     * chWithColor = [[ch,color],[ch,color]...]
     */     
        now = Date.now()              
        for(i in chWithColor){
            ch = chWithColor[i][0]
            color = chWithColor[i][1]
            path = this.findPathById('#'+ch.name)
            if(path == null){
                this.paths.append('path')
                    .data([ch.data]) // MIGHT NEED BRACKETS
                    .style('stroke',color)
                    .attr('id',ch.name)
                    .attr('class','group')
                    
            }
        }
        this.x.domain([now-(10*1000),now])//ten second domain
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
        if(path!=null){
            path.remove()
        }	
        this.updateGraph()
        
    }
    this.updateGraph = () => {
        this.paths.selectAll('path').attr('d',this.line)
        this.xaxis.transition()
            .duration(0)
            .ease('linear')
            .call(this.x.axis)
    }    

}

