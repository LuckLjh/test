//先cmd里cd到这个js所在的文件夹，然后直接node getCode.js运行就ok了

var fs =require("fs");

function delete1(path){

  var dirList = fs.readdirSync(path);
  dirList.forEach(function(item){
  	var inpath = path+'/'+item;
  	if(fs.statSync(inpath).isDirectory() == false){
  		fs.readFile(inpath,"utf8",function(err,data){
             var reg = "noDefinePart",
                 pos = data.indexOf(reg);
             if (pos > 0){
             	var pos1 = data.indexOf(reg);
             	data = data.slice(pos1+12);
             	var pos2 = data.indexOf(reg);
             	data = data.slice(0,pos2-2)
             }
             fs.writeFileSync(inpath,data)

  		})
  	}
  })
}
delete1("jstest")

/*copy("demo","srcCode")
 function walk(path){
  var srcList = fs.readdirSync("srcCode");
  var dirList = fs.readdirSync(path);
  dirList.forEach(function(item){
    var inpath = path+'/'+item,
        srcpath = 'srcCode' + '/' + item;
    if(fs.statSync(inpath).isDirectory()){
      var indir = fs.readdirSync(inpath);
      indir.forEach(function(item1){
                
         if(item1[item1.length-1]=="p") {
            fs.readFile(inpath+'/'+item1,"utf8",function(err,data){
				var reg=new RegExp("<\%","g");
				var data = data.replace(reg,"&lt%");
                var data1 = data.split("\n");
	            data1.unshift("<%@ page language='java' contentType='text/html; charset=UTF-8' pageEncoding='UTF-8'%>")
                var data2 = data1.join("\n");
                fs.writeFileSync(srcpath+'/'+item1,data2)
            })
         }else{
           fs.writeFileSync(srcpath+'/'+item1, fs.readFileSync(inpath+'/'+item1));
         }
      })
    }
  });
}  


 function copy(demoPath,codePath){
  var codeDirList = fs.readdirSync(codePath),
      demoDirList = fs.readdirSync(demoPath);
    codeDirList.forEach(function(item){
    var url = codePath+"/"+item;
    clear(url);
  })
  demoDirList.forEach(function(item){
    var codeDir = codePath+"/"+item;
    if (fs.statSync(demoPath+"/"+item).isDirectory()){
      fs.mkdirSync(codeDir, 0755);
    }
    
  })
  walk(demoPath)
  
}

function clear(url){
  if (fs.statSync(url).isDirectory()){
    var innerDir = fs.readdirSync(url);
    innerDir.forEach(function(item){
      var path = url+"/"+item; 
      fs.unlinkSync(path);          
    })
    fs.rmdir(url)
  }else{
    fs.unlinkSync(url);
  }
}
*/
