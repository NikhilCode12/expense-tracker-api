package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.AuthDTO;
import in.nikhilsh.expensetracker.dto.ProfileDTO;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.ProfileRepository;
import in.nikhilsh.expensetracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerUser(ProfileDTO profileDTO){
        Profile newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        String activationLink = activationURL + "/api/v0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate Your Expense Tracker Account";

        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0;">
                <div style="max-width: 600px; margin: 40px auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                    <h2 style="text-align: center; color: #333;">Welcome, %s!</h2>
        
                    <p>Thank you for signing up with <strong>Expense Tracker</strong>.</p>
                    <p>Please confirm your email address to activate your account.</p>
        
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s"
                           style="background-color: #007BFF; color: #ffffff; text-decoration: none; 
                                  padding: 12px 24px; border-radius: 6px; font-weight: 600; 
                                  font-size: 16px; display: inline-block;">
                            Activate Account
                        </a>
                    </div>
        
                    <p>If you didn’t sign up for this account, you can safely ignore this email.</p>
        
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="font-size: 12px; color: #888; text-align: center;">Expense Tracker © 2025</p>
                </div>
            </body>
            </html>
        """, newProfile.getFullName(), activationLink);


        emailService.sendMail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    public boolean activateProfile(@RequestParam String activationToken){
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }).orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
                .map(Profile::getIsActive)
                .orElse(false);
    }

    public Profile getCurrentProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName()).orElseThrow(
                ()-> new UsernameNotFoundException("Profile not found with email: " + authentication.getName())
        );
    }

    public Profile toEntity(ProfileDTO dto){
        return Profile.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .profileImageUrl(dto.getProfileImageUrl())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(Profile entity){
        return ProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .password(passwordEncoder.encode(entity.getPassword()))
                .profileImageUrl(entity.getProfileImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProfileDTO getPublicProfile(String email){
        Profile currentProfile = null;
        if(email == null){
            currentProfile = getCurrentProfile();
        } else{
            currentProfile = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        return toDTO(currentProfile);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));

            // generate JWT Token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e){
            throw new RuntimeException("Invalid email or password");
        }
    }
}
