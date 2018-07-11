function FFTGraph(width, height, id){
    this.desiredSize = 2**9 //powers of 2 work best
    this.difs = new Array(this.desiredSize/4) //rolling average to calculate sample rate
    this.data = d3.range(1).map(function(){
        return [0,0]
    })
    this.mag_list = []
    this.x = d3.scale.linear()
        .domain([0,100])
        .range([0,width])
    this.y = d3.scale.linear()
        .domain([0,250])
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
    this.xaxis = this.svg.append('g')
        .attr('class','x axis')
        .attr('transform','translate(0,'+height+')')
        .call(this.x.axis=d3.svg.axis().scale(this.x).orient('top'))
    this.yaxis = this.svg.append('g')
        .attr('class','y axis')
        .call(this.y.axis = d3.svg.axis().scale(this.y).orient('right'))
    
    this.paths = this.svg.append('g')
    this.path = this.paths.append('path')
        .data([this.data])
        //.style('stroke','red')
        //.style('fill','none')
    this.lastDate = new Date(Date.now())//used for difs
    this.updateData = (event) =>{
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
            this.mag_list = []        
        }
        
    }
}