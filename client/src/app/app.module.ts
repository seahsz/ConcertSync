import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// Angular Material Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import {MatCheckboxModule} from '@angular/material/checkbox';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

// Components
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { GroupFinderComponent } from './components/group-finder/group-finder.component';
import { SetlistExplorerComponent } from './components/setlist-explorer/setlist-explorer.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ConcertDetailsComponent } from './components/concert-details/concert-details.component';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ConcertService } from './services/concert.service';
import { RegisterComponent } from './components/register/register.component';
import { AuthService } from './services/auth.service';
import { LoginComponent } from './components/login/login.component';
import { EmailVerificationComponent } from './components/email-verification/email-verification.component';
import { SharedNavbarComponent } from './components/shared/shared-navbar/shared-navbar.component';
import { SharedFooterComponent } from './components/shared/shared-footer/shared-footer.component';
import { authInterceptor } from './services/auth.interceptor';
import { UserProfileService } from './services/user-profile.service';
import { GroupService } from './services/group.service';
import { AuthGuard } from './services/auth.guard';
import { GroupCreationComponent } from './components/group-creation/group-creation.component';

// Routes configuration
const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: EmailVerificationComponent },
  { path: 'login', component: LoginComponent },
  { path: 'groups', component: GroupFinderComponent, canActivate: [AuthGuard] },
  { path: 'create-group/:id', component: GroupCreationComponent, canActivate: [AuthGuard] },
  { path: 'setlists', component: SetlistExplorerComponent }, // Not implmeneted
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'concerts/:id', component: ConcertDetailsComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GroupFinderComponent,
    SetlistExplorerComponent,
    ProfileComponent,
    ConcertDetailsComponent,
    RegisterComponent,
    LoginComponent,
    EmailVerificationComponent,
    SharedNavbarComponent,
    SharedFooterComponent,
    GroupCreationComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),

    // Material Modules
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSnackBarModule,
    MatDatepickerModule,
    MatProgressSpinnerModule,
    MatNativeDateModule,
    MatDividerModule,
    MatMenuModule,
    MatCheckboxModule,
    MatOptionModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  providers: [provideHttpClient(withInterceptors([authInterceptor])), ConcertService, AuthService, UserProfileService,
    GroupService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }