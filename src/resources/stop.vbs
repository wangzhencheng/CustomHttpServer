Set ws = CreateObject("Wscript.Shell")
p="%s"
ws.run "cmd /c java SimpleHttp get http://localhost:"&p&"/exit?password=admin",0,true
MsgBox "stopped!"
