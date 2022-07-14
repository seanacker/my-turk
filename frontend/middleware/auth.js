export default function ({ route, _, redirect }) {
  if (process.client && localStorage.token === '' && route.name !== 'index') {
    console.log('redirect to login')
    return redirect('/')
  }
}
