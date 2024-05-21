import {useAuth0} from "@auth0/auth0-react";

export default function DocOverview() {
  const { getAccessTokenSilently, logout } = useAuth0();

  async function printAccessToken() {
    const accessToken = await getAccessTokenSilently({
      authorizationParams: {audience: "https://knowledge-base-api/"},
    });
    console.log(accessToken);
  }

  return (
    <>
      <button onClick={() => printAccessToken()}>Print Access Token</button>
      <button onClick={() => logout({ logoutParams: { returnTo: 'http://localhost:5173' } })}>Logout</button>
    </>
  );
}
