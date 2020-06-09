//simple string stack wrapper for convenience
class Stack { 
	constructor() { this.items = [];} 
    push(e){this.items.push(e);} 
    pop(){if(isEmpty())return ""; return this.items.pop();} 
    isEmpty() {return this.items.length == 0;} 
    peek(){if(isEmpty())return ""; return this.items[this.items.length - 1];}
    toString(vs){
        if(typeof vs==='undefined')vs='\n';
        let r="";
        for(let i=0; i<this.items.length; ++i)
            r+=this.items[i]+vs;
        return r;
    }
} 
