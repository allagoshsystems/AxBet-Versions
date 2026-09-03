const firebaseConfig = {
    apiKey: "AIzaSyCDTMuH0UbB5CYAxgfFj97UtowM2drCN_8",
    projectId: "axbetorg",
};
const url = `https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${firebaseConfig.apiKey}`;
fetch(url, {
  method: 'POST',
  body: JSON.stringify({email: 'test_admin_debug@m20m.com', password: 'password123', returnSecureToken: true}),
  headers: {'Content-Type': 'application/json'}
})
.then(res => res.json())
.then(data => {
  const token = data.idToken;
  console.log("Token:", token ? "Got token" : data);
  if (!token) return;
  // Now try to fetch bets
  const dbUrl = `https://firestore.googleapis.com/v1/projects/${firebaseConfig.projectId}/databases/(default)/documents/bets`;
  return fetch(dbUrl, {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json()).then(console.log);
})
.catch(err => console.error(err));
