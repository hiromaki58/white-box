import './App.css';
import Amplify from 'aws-amplify';
import Chat from './Chat'

function App() {
  //Cognito, AppSync co-working setting
  Amplify.configure({
    Auth: {
      region: process.env.REACT_APP_REGION,
      userPoolId: process.env.REACT_APP_USERPOOLID,
      userPoolWebClientId: process.env.REACT_APP_USERPOOLWEBCLIENTID
    },
    aws_appsync_graphqlEndpoint: process.env.REACT_APP_APPSYNC,
    aws_appsync_region: process.env.REACT_APP_REGION,
    aws_appsync_authenticationType: "AMAZON_COGNITO_USER_POOLS",
    aws_appsync_apiKey: "null"
  });
  return (
    <div className="App">
      <header className="App-header">
        <h1>Web chat</h1>
        <Chat username="username" serviceid="serviceid" />
      </header>
    </div>
  );
}

export default App;
