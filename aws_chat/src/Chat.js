import React, { useState, useEffect } from 'react';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import SendIcon from '@mui/icons-material/Send';
import { API } from 'aws-amplify';
import gql from 'graphql-tag';
import { format } from 'date-fns';

const Chat = (props) => {
  const username = props.username;
  const serviceid = props.serviceid;

  const [message, setMessage] = useState("");
  const [chatdata, setChatdata] = useState();

  const queryGetChat = gql`
    query queryChatdataByServiceid($serviceid: String!) {
      queryChatdataByServiceid(serviceid: $serviceid) {
        items {
          datetime
          user
          message
        }
      }
    }
  `;

  const getChat = async () => {
    const res = await API.graphql({
      query: queryGetChat,
      variables: { serviceid: serviceid },
      authMode: "AMAZON_COGNITO_USER_POOLS"
    });
    setChatdata(res.data.queryChatdataByServiceid.items);
  };

  const queryPutChat = gql`
    mutation putChatdata(
      $serviceid: String!,
      $datetimeuser: String!,
      $datetime: String!,
      $message: String!,
      $user: String!
    ) {
      putChatdata(input: {
        serviceid: $serviceid,
        datetimeuser: $datetimeuser,
        datetime: $datetime,
        message: $message,
        user: $user
      }) {
        serviceid
      }
    }
  `;

  const putChat = async () => {
    const dt = (new Date()).toISOString();
    await API.graphql({
      query: queryPutChat,
      variables: {
        serviceid: serviceid,
        datetimeuser: dt + "#" + username,
        datetime: dt,
        message: message,
        user: username
      },
      authMode: "AMAZON_COGNITO_USER_POOLS"
    });
    setMessage("");
  };

  const querySubscribePutChat = gql`
    subscription onPutChatdata($serviceid: String!) {
      onPutChatdata(serviceid: $serviceid) {
        serviceid
      }
    }
  `;

  const subscribePutChat = async () => {
    const subscription = await API.graphql({
      query: querySubscribePutChat,
      variables: { serviceid: serviceid },
      authMode: 'AMAZON_COGNITO_USER_POOLS'
    }).subscribe({
      next: (response) => {
        if (response.value.data.onPutChatdata.serviceid === serviceid) {
          getChat();
        }
      },
      error: (err) => console.log(err)
    });
    return () => subscription.unsubscribe();
  };

  useEffect(() => {
    getChat();
    subscribePutChat();
  }, []);

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="body2" component="h3" style={{ fontWeight: "bold" }}>Messages</Typography>
      <Divider sx={{ my: 1 }} />
      <Grid container spacing={2}>
        <Grid item xs={12} md={6} lg={12}>
          {chatdata?.map((row) => (
            <React.Fragment key={row.datetimeuser}>
              <Stack direction="row" justifyContent="flex-start" alignItems="flex-start" spacing={1}>
                <Typography variant="caption" component="span" color="text.secondary">{row.user.split("@")[0]}</Typography>
                <Typography variant="caption" component="span" color="text.secondary">{format(Date.parse(row.datetime), "yyyy-MM-dd HH:mm")}</Typography>
              </Stack>
              <Typography variant="caption" component="div" sx={{ mb: 1 }} style={{ whiteSpace: "pre-line" }}>{row.message}</Typography>
              <Divider sx={{ my: 1 }} />
            </React.Fragment>
          ))}
        </Grid>
        <Grid item xs={12} md={6} lg={12}>
          <TextField id="message" placeholder="Type message" value={message} onChange={e => setMessage(e.target.value)} size="small" multiline minRows={1} fullWidth />
          <Button
            onClick={() => putChat()}
            variant="contained"
            size="small"
            color="success"
            sx={{ my: 2 }}
            disabled={!message}
            startIcon={<SendIcon />}
          >
            コメント
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Chat;