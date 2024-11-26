export class ConstList {
    /**
     * URL to get the script
     * @returns URL
     */
    static inboundJsonUrl() {
        return "https://fakeserverhiromaki580113.on.drv.tw/www.fakeserver.com/"; //local test which is fake web server built up on the Google drive
    }
    /**
     * Callback function name of the JSONP file
     * @returns Callback function name
     */
    static callbackFName() {
        return "chatlp";
    }
    /**
     * Return the JSON POST server address
     * @returns URL
     */
    static outboundJsonUrl() {
        return "https://jsonplaceholder.typicode.com/posts"; //local test
    }
    /**
     * Return the user terms of use address
     * @returns URL
     */
    static regulationUrl() {
        return "https://www.yahoo.co.jp/";
    }
    /**
     * Target of the document.getElementById
     * @returns startting point
     */
    static startPoint() {
        return "root";
    }
    /**
     * PC CSS link
     * @returns PC CSS link
     */
    static pcCss() {
        return "/base-pc.8848e24a.css"; //local test
    }
    /**
     * Smartphone CSS link
     * @returns Smartphone CSS link
     */
    static spCss() {
        return "/base-sp.3ff27c20.css"; //local test
    }
    /**
     * Enter icon link
     * @returns Enter icon link
     */
    static enterBtnLink() {
        return "/ico_enter_01.ed6e504d.svg"; //local test
    }
    /**
     * Top icon link
     * @returns Top icon link
     */
    static topIcoLink() {
        return "/ico_top_01.81a17eb2.svg"; //local test
    }
    /**
     * Time stamp key
     * @param chatId chatId
     * @returns time stamp key
     */
    // static timestampKey(chatId): string{
    //   const key = "web3_chat_" + chatId;
    //   return key;
    // }
    /**
     * Time to show the next object
     * @returns waiting time
     */
    static waitTime() {
        return 1500;
    }
}
