// << CONNECT >>(UUID)>>>>(Name)<< CONNECT >>

/**
 * 用户注册协议消息的左半部分。
 * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
 */
const CONNECTING_GREET_LEFT_HALF = "<< CONNECT >>";

/**
 * 用户注册协议消息的中间部分。
 * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
 */
const CONNECTING_GREET_MIDDLE_HALF = ">>>>>";

/**
 * 用户注册协议消息的右半部分。
 * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
 */
const CONNECTING_GREET_RIGHT_HALF = "<< CONNECT >>";

/**
 * 客户端通知注销的请求头。
 */
const NOTIFY_LOGOFF_HEADER = "<< LOGOFF >>";

// << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)
/**
 * 信息包的左半部分。
 * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
 */
const MESSAGE_HEADER_LEFT_HALF = "<< MESSAGE >>> <<<<";

/**
 * 信息包的中间部分。
 * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
 */
const MESSAGE_HEADER_MIDDLE_HALF = ">>>>>";

/**
 * 信息包的右半部分。
 * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
 */
const MESSAGE_HEADER_RIGHT_HALF = " << MESSAGE >>";

/**
 * 调试模式的信号字。目前是让服务器把用户档案打印出来。
 */
const DEBUG_MODE_const = "/// DEBUG ///";

/**
 * 调试模式的信号字。目前是让服务器把用户档案打印出来。
 */
const BROADCAST_MESSAGE_UUID = "SERVER";

// <<< DUP ? >>> (Name)
/**
 * 用户名查询服务请求头。
 */
const CHECK_DUPLICATE_REQUEST_HEADER = "<<< DUP ? >>> ";

/**
 * 用户名查询服务响应 - 用户名已经占用
 */
const USER_NAME_DUPLICATED = ">>> DUPLICATED <<< ";

/**
 * 用户名查询服务响应 - 用户名没有占用
 */
const USER_NAME_NOT_EXIST = "<<< Clear >>>";

let ws;

let initWS = (host, port) => {
    ws = new WebSocket("ws://" + host + ":" + port + "/acmcs/wshandler");

    ws.onopen = function(){
    };

    ws.onmessage = function(message){
        document.getElementById("chatlog").textContent += message.data + "\n";
    };
}

function postToServer(){
    ws.send(document.getElementById("msg").value);
    document.getElementById("msg").value = "";
}

function closeConnect(){
    ws.close();
}