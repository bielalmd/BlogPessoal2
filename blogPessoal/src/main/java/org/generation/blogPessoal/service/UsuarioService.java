package org.generation.blogPessoal.service;
import java.nio.charset.Charset;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import java.time.LocalDate;
import java.time.Period;
import org.generation.blogPessoal.Repository.UsuarioRepository;
import org.generation.blogPessoal.model.UserLogin;
import org.generation.blogPessoal.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	public Usuario cadastrarUsuario(Usuario usuario) {
		
		if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST, "Usuário já existe!", null);

		 int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
		
		 if(idade < 18)
			throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Usuário menor de 18 anos", null);

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String senhaEncoder = encoder.encode(usuario.getSenha());
		usuario.setSenha(senhaEncoder);

		return (usuarioRepository.save(usuario));
	}

	public Optional<UserLogin> Logar(Optional<UserLogin> user) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuario = usuarioRepository.findByUsuario(user.get().getUsuario());

		if (usuario.isPresent()) {
			if (encoder.matches(user.get().getSenha(), usuario.get().getSenha())) {

				String auth = user.get().getUsuario() + ":" + user.get().getSenha();
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodeAuth);
				
				
				user.get().setId(usuario.get().getId());
				user.get().setNome(usuario.get().getNome());
				user.get().setSenha(usuario.get().getSenha());
				user.get().setTipo(usuario.get().getTipo());
				user.get().setFoto(usuario.get().getFoto());
				user.get().setToken(authHeader);
				
				
				return user;

			}
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos!", null);

	}
public Optional<Usuario> atualizarUsuario(Usuario usuario) {
		
		if(usuarioRepository.findById(usuario.getId()).isPresent()) {
			
			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			
			if( buscaUsuario.isPresent() ){

				if(buscaUsuario.get().getId() != usuario.getId())
					throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
			}


			BCryptPasswordEncoder enconder = new BCryptPasswordEncoder();
			
			String senhaEnconder = enconder.encode(usuario.getSenha());
			usuario.setSenha(senhaEnconder);
			
			return Optional.of(usuarioRepository.save(usuario));
			
		}else {
			
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!", null);
		}
	}

}
