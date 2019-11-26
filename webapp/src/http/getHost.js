
export const getHost = () => {
  let host;
  if (process.env.NODE_ENV === 'development') {
    host = 'http://localhost:8080'
  } else {
    // Make api request at the same host as the client
    host = `${window.location.protocol}//${window.location.host}:8080`;
  }
  return host;
}

export default getHost;