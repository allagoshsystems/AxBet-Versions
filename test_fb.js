const firebaseConfig = {
    apiKey: "AIzaSyCDTMuH0UbB5CYAxgfFj97UtowM2drCN_8",
    projectId: "axbetorg",
};
const url = `https://firestore.googleapis.com/v1/projects/${firebaseConfig.projectId}/databases/(default)/documents/bets`;
fetch(url)
  .then(res => res.json())
  .then(data => console.log(JSON.stringify(data, null, 2)))
  .catch(err => console.error(err));
