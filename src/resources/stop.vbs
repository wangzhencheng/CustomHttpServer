Set ws = CreateObject("Wscript.Shell") 
ws.run "cmd /c java SimpleHttp get http://localhost:%s/exit?password=admin",0,true
MsgBox "stopped!"
