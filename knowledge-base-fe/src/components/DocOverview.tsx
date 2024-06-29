import {useAuth0} from '@auth0/auth0-react';

export default function DocOverview() {
  const { getAccessTokenSilently, logout } = useAuth0();

  return (
    <>
      <button onClick={() => getAccessTokenSilently().then(token => console.log(token))}>Print Access Token</button>
      <button onClick={() => logout({ logoutParams: { returnTo: 'http://localhost:5173' } })}>Logout</button>
    </>
  );
}
