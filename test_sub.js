const firebaseConfig = {
    apiKey: "AIzaSyCDTMuH0UbB5CYAxgfFj97UtowM2drCN_8",
    projectId: "axbetorg",
};
const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${firebaseConfig.apiKey}`;
fetch(url, {
  method: 'POST',
  body: JSON.stringify({email: 'test_admin_debug@m20m.com', password: 'password123', returnSecureToken: true}),
  headers: {'Content-Type': 'application/json'}
})
.then(res => res.json())
.then(data => {
  const token = data.idToken;
  const dbUrl = `https://firestore.googleapis.com/v1/projects/${firebaseConfig.projectId}/databases/(default)/documents/users/J4tLECMvGEQQuB0GXqv4N9aCFKY2/bets`;
  return fetch(dbUrl, {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json()).then(d => console.log("Bets:", JSON.stringify(d, null, 2)));
})
.catch(err => console.error(err));
