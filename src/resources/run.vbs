Set ws = CreateObject("Wscript.Shell") 

f="service.jar"
if CreateObject("Scripting.FileSystemObject").FileExists(f) Then 
	ws.run "cmd /c java -Dfile.encoding=utf-8 -jar service.jar --port=%s --password=admin",0
	ws.run "%s http://localhost:%s/index.html" ,1
else
	MsgBox "service.jar not exits",,"Info"
end if

