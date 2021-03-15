package com.enigma.creditscoringapi.controllers;

import com.enigma.creditscoringapi.entity.Role;
import com.enigma.creditscoringapi.entity.Users;
import com.enigma.creditscoringapi.entity.enums.ERole;
import com.enigma.creditscoringapi.models.JwtResponse;
import com.enigma.creditscoringapi.models.LoginRequest;
import com.enigma.creditscoringapi.models.MessageResponse;
import com.enigma.creditscoringapi.models.SignUpRequest;
import com.enigma.creditscoringapi.repository.UsersRepository;
import com.enigma.creditscoringapi.security.jwt.JwtUtils;
import com.enigma.creditscoringapi.security.service.UserDetailsImpl;
import com.enigma.creditscoringapi.services.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UsersRepository repository;

    @Autowired
    RoleService service;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getDateRegister(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest request) throws IOException {

        if (repository.existsByUsername(request.getUsername())){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already use"));
        }

        if (repository.existsByEmail(request.getEmail())){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already use"));
        }

        Users user = modelMapper.map(request, Users.class);

        Set<String> strRoles = request.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role staff = service.findRoleByName(ERole.STAFF);
            roles.add(staff);
        } else {
            Role supervisor = service.findRoleByName(ERole.SUPERVISOR);
            roles.add(supervisor);
        }

        user.setRoles(roles);
        repository.save(user);

        return ResponseEntity.ok(new MessageResponse("User Registered successfully"));
    }
}