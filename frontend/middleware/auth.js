export default function ({ route, _, redirect }) {
  if (process.client && localStorage.token === '' && route.name !== 'index') {
    return redirect('/')
  }
}
