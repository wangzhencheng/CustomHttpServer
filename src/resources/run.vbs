Set ws = CreateObject("Wscript.Shell") 

p="%s"
f="service.jar"
if CreateObject("Scripting.FileSystemObject").FileExists(f) Then 
	ws.run "cmd /c java -Dfile.encoding=utf-8 -jar service.jar --port="&p&" --password=admin",0
	ws.run "%s http://localhost:"&p&"/index.html" ,1
else
	MsgBox "service.jar not exits",,"Info"
end if

