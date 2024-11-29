import { ConstList } from "./common/ConstList";
// import { VarList } from "./common/VarList";
// import { CtrlUtility } from "./controller/CtrlUtility";
// import { DomainController } from "./controller/DomainController";
// import { MainController } from "./controller/MainController";
// import { TimeStampController } from "./controller/TimeStampController";
// import { OutboundProps } from "./model/OutboundJson";
// import { MainForm } from "./view/MainForm";
// import { Redirect } from "./view/Redirect";

// const inboundUrl = ConstList.inboundJsonUrl();
// const cbName = ConstList.callbackFName();

/**
 * Create the interface will be linked to the global window
 */
interface ExposedFunction {
  initGameLp(chatId: string): void;
  sendGameInfo(chatId: string): void;
}

/**
 * Set the interface to the global window
 */
declare global {
  interface Window {
    ef: ExposedFunction;
  }
}

/**
 * Immediate function that is  the initial function to start the typescript code
 */
const localFunction: ExposedFunction = (() => {
  //Initiaize the data set which will be sent back to server
  // let outbound: OutboundProps = {chat_id: "temp", answers: []};

  return {
    /**
     * Fetch the JSONP data and show in the HTML
     * @param chatId JSONP data ID
     */
    initGameLp: (chatId: string) => {
      // //Set the target URL
      // const rootUrl = inboundUrl + cbName + chatId + '.js';                                              //local test
      // // const rootUrl = inboundUrl + chatId + '&callback=' + ConstList.callbackFName();        //integration test

      // //Get the JSONP data
      // const controller = new MainController(rootUrl, cbName);
      // //Set the chat ID to the global variable
      // VarList.setChatId(chatId);

      // //Set the event
      // controller.on('getJsonp', () => {
      //   const root = document.getElementById(ConstList.startPoint());
      //   if(!root){
      //     throw new Error('The root element is null.');
      //   }
      //   //Render HTML
      //   else{
      //     if(controller.status !== 1){
      //       const redirecForm = new Redirect(root, controller, outbound);
      //       redirecForm.render(controller.redirectUrl);
      //     }
      //     else{
      //       const mainForm = new MainForm(root, controller, outbound);
      //       mainForm.render();
      //     }
      //   }
      // });
      // // Store original JSON data
      // controller.fetch();

      // // Set the time stamp
      // const timestampKey = ConstList.timestampKey(chatId);
      // const tsc = new TimeStampController();
      // tsc.setTimeStamp(timestampKey);
    },

    /**
     * To send the time stamp when the user access to the chaLp
     */
    sendGameInfo: (chatId: string) => {
      // const timestampKey = ConstList.timestampKey(chatId);
      // const cookieTimeStamp = document.cookie.split(";").find(cookie => cookie.startsWith(timestampKey));

      // // Check if the last visited timestamp is stored in the localStorage
      // if(localStorage.getItem(timestampKey) !== null && localStorage.getItem(timestampKey) !== undefined){
      //   let today = new Date();
      //   const retrivedValue = localStorage.getItem(timestampKey);
      //   const threshholdDate = new Date(today.setMonth(today.getMonth() - 6));
      //   /**
      //    * If the retrived date is old more than half year since today when the user access to the chatLp
      //    * delete the timestamp
      //    */
      //   if(retrivedValue !== null && retrivedValue !== undefined){
      //     const retrivedDate = new Date(retrivedValue);
      //     if(retrivedDate < threshholdDate){
      //       localStorage.removeItem(timestampKey);
      //     }
      //     else{
      //       // Make up the JSON date wiht the retrived time stamp
      //       const outboundTimeStamp = `{"key":"${timestampKey}", "value":"${retrivedValue.toString()}"}`;
      //       CtrlUtility.sendData<string>(ConstList.outboundJsonUrl(), JSON.parse(outboundTimeStamp));
      //       // Remove the time stamp in the local storage
      //       localStorage.removeItem(timestampKey);
      //     }
      //   }
      // }
      // else if(cookieTimeStamp !== null && cookieTimeStamp !== undefined){
      //   // Make up the JSON date wiht the retrived time stamp
      //   const cookieValue = cookieTimeStamp.split('=').at(1);
      //   if(cookieValue !== undefined){
      //     const outboundTimeStamp = `{"key":"${timestampKey}", "value":"${cookieValue.toString()}"}`;
      //     CtrlUtility.sendData<string>(ConstList.outboundJsonUrl(), JSON.parse(outboundTimeStamp));
      //   }
      //   // Remove the cookie
      //   const domainController = new DomainController();
      //   let domainInfo = domainController.parse(document.domain);
      //   document.cookie = `${timestampKey}=; domain=${domainInfo.domain}; max-age=0`;
      // }
      // else {
      //   /**
      //    * In case of there is no time stamp, do nothing.
      //    */
      // }
    }
  };
})();

//Set the local function to the Window property
window.ef = localFunction;